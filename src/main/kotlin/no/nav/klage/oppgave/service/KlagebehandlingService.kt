package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.Kilde
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.oppgavekopi.IdentType
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjon
import no.nav.klage.oppgave.domain.oppgavekopi.VersjonIdent
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.repositories.MottakRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class KlagebehandlingService(
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val mottakRepository: MottakRepository,
    private val hjemmelService: HjemmelService,
    private val tilgangService: TilgangService,
    private val overfoeringsdataParserService: OverfoeringsdataParserService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private const val KLAGEINSTANS_PREFIX = "42"
    }

    private fun checkTilgang(klagebehandling: Klagebehandling) {
        klagebehandling.foedselsnummer?.let {
            tilgangService.verifySaksbehandlersTilgangTil(it)
        }
    }

    fun getOppgaveIderForKlagebehandling(klagebehandlingId: UUID): List<Long> =
        mottakRepository.getOne(klagebehandlingRepository.getOne(klagebehandlingId).mottakId).oppgavereferanser.map { it.oppgaveId }

    fun getKlagebehandling(klagebehandlingId: UUID): Klagebehandling =
        klagebehandlingRepository.getOne(klagebehandlingId).also { checkTilgang(it) }

    fun getKvalitetsvurdering(klagebehandlingId: UUID): Kvalitetsvurdering? =
        klagebehandlingRepository.getOne(klagebehandlingId).also { checkTilgang(it) }.kvalitetsvurdering

    fun fetchMottakForOppgaveKopi(oppgaveId: Long): List<Mottak> =
        mottakRepository.findByOppgavereferanserOppgaveId(oppgaveId)

    fun updateKvalitetsvurdering(
        klagebehandlingId: UUID,
        kvalitetsvurderingInput: KvalitetsvurderingInput
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        klagebehandling.createOrUpdateKvalitetsvurdering(kvalitetsvurderingInput)
        return klagebehandling
    }

    fun getKlagebehandlingForOppgaveId(oppgaveId: Long) =
        fetchMottakForOppgaveKopi(oppgaveId).mapNotNull {
            klagebehandlingRepository.findByMottakId(it.id)
        }.filter {
            it.avsluttet == null
        }


    fun connectOppgaveKopiToKlagebehandling(oppgaveKopierOrdererByVersion: List<OppgaveKopiVersjon>): List<Pair<Klagebehandling, Mottak>> {
        val lastVersjon = oppgaveKopierOrdererByVersion.first()

        if (lastVersjon.tildeltEnhetsnr.startsWith(KLAGEINSTANS_PREFIX) && (lastVersjon.oppgavetype == "BEH_SAK_MK" || lastVersjon.oppgavetype == "BEH_SAK")) {
            val mottakSomHarPaagaaendeKlagebehandlinger =
                fetchMottakForOppgaveKopi(lastVersjon.id).filter {
                    klagebehandlingRepository.findByMottakId(it.id)?.avsluttet == null
                }
            return if (mottakSomHarPaagaaendeKlagebehandlinger.isEmpty()) {
                listOf(createNewMottakAndKlagebehandling(oppgaveKopierOrdererByVersion))
            } else {
                mottakSomHarPaagaaendeKlagebehandlinger.map { updateMottak(it, oppgaveKopierOrdererByVersion) }
            }
        } else {
            return emptyList()
        }
    }

    private fun createNewMottakAndKlagebehandling(oppgaveKopierOrdererByVersion: List<OppgaveKopiVersjon>): Pair<Klagebehandling, Mottak> {
        val lastVersjon = oppgaveKopierOrdererByVersion.first()
        requireNotNull(lastVersjon.ident)
        requireNotNull(lastVersjon.behandlingstype)

        val overfoeringsdata = overfoeringsdataParserService.parseBeskrivelse(lastVersjon.beskrivelse ?: "")

        val createdMottak = mottakRepository.save(
            Mottak(
                tema = mapTema(lastVersjon.tema),
                sakstype = mapSakstype(lastVersjon.behandlingstype),
                referanseId = lastVersjon.saksreferanse,
                foedselsnummer = lastVersjon.ident.folkeregisterident,
                organisasjonsnummer = mapOrganisasjonsnummer(lastVersjon.ident),
                hjemmelListe = mapHjemler(lastVersjon),
                avsenderSaksbehandlerident = findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopierOrdererByVersion)?.endretAv
                    ?: overfoeringsdata?.saksbehandlerWhoMadeTheChange,
                avsenderEnhet = findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopierOrdererByVersion)?.endretAvEnhetsnr
                    ?: overfoeringsdata?.enhetOverfoertFra ?: lastVersjon.opprettetAvEnhetsnr,
                oversendtKaEnhet = findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopierOrdererByVersion)?.tildeltEnhetsnr
                    ?: overfoeringsdata?.enhetOverfoertTil ?: lastVersjon.tildeltEnhetsnr,
                oversendtKaDato = findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopierOrdererByVersion)?.endretTidspunkt?.toLocalDate()
                    ?: overfoeringsdata?.datoForOverfoering,
                fristFraFoersteinstans = lastVersjon.fristFerdigstillelse,
                beskrivelse = lastVersjon.beskrivelse,
                status = lastVersjon.status.name,
                statusKategori = lastVersjon.statuskategori().name,
                tildeltEnhet = lastVersjon.tildeltEnhetsnr,
                tildeltSaksbehandlerident = lastVersjon.tilordnetRessurs,
                journalpostId = lastVersjon.journalpostId,
                journalpostKilde = lastVersjon.journalpostkilde,
                kilde = Kilde.OPPGAVE,
                oppgavereferanser = mutableListOf(
                    Oppgavereferanse(
                        oppgaveId = lastVersjon.id
                    )
                )
            )
        )

        val createdKlagebehandling = klagebehandlingRepository.save(
            Klagebehandling(
                foedselsnummer = createdMottak.foedselsnummer,
                tema = createdMottak.tema,
                sakstype = createdMottak.sakstype,
                referanseId = createdMottak.referanseId,
                innsendt = null,
                mottattFoersteinstans = null,
                avsenderEnhetFoersteinstans = createdMottak.avsenderEnhet,
                avsenderSaksbehandleridentFoersteinstans = createdMottak.avsenderSaksbehandlerident,
                mottattKlageinstans = createdMottak.oversendtKaDato,
                startet = null,
                avsluttet = null,
                frist = createdMottak.fristFraFoersteinstans,
                tildeltSaksbehandlerident = createdMottak.tildeltSaksbehandlerident,
                tildeltEnhet = createdMottak.tildeltEnhet,
                mottakId = createdMottak.id,
                vedtak = null,
                kvalitetsvurdering = null,
                hjemler = createdMottak.hjemler().map { hjemmelService.generateHjemmelFromText(it) }.toMutableSet(),
                saksdokumenter = if (createdMottak.journalpostId != null) {
                    mutableSetOf(Saksdokument(referanse = createdMottak.journalpostId!!))
                } else {
                    mutableSetOf()
                },
                kilde = Kilde.OPPGAVE
            )
        )
        logger.debug("Created behandling ${createdKlagebehandling.id} with mottak ${createdMottak.id} for oppgave ${lastVersjon.id}")
        return Pair(createdKlagebehandling, createdMottak)
    }


    private fun updateMottak(
        mottak: Mottak,
        oppgaveKopierOrdererByVersion: List<OppgaveKopiVersjon>
    ): Pair<Klagebehandling, Mottak> {
        logger.debug("Updating mottak")

        val lastVersjon = oppgaveKopierOrdererByVersion.first()
        requireNotNull(lastVersjon.ident)
        requireNotNull(lastVersjon.behandlingstype)

        //TODO: Legge til nytt saksdokument hvis journalpostId er oppdatert?
        mottak.apply {
            tema = mapTema(lastVersjon.tema)
            sakstype = mapSakstype(lastVersjon.behandlingstype)
            referanseId = lastVersjon.saksreferanse
            foedselsnummer = lastVersjon.ident.folkeregisterident
            organisasjonsnummer = mapOrganisasjonsnummer(lastVersjon.ident)
            hjemmelListe = mapHjemler(lastVersjon)
            fristFraFoersteinstans = lastVersjon.fristFerdigstillelse
            beskrivelse = lastVersjon.beskrivelse
            status = lastVersjon.status.name
            statusKategori = lastVersjon.statuskategori().name
            journalpostId = lastVersjon.journalpostId
            journalpostKilde = lastVersjon.journalpostkilde
            //TODO: Bør dise oppdateres?
            tildeltEnhet = lastVersjon.tildeltEnhetsnr
            tildeltSaksbehandlerident = lastVersjon.tilordnetRessurs
            //oversendtKaEnhet = mapMottakerEnhet(oppgaveKopierOrdererByVersion)
            //oversendtKaDato = mapOversendtKaDato(oppgaveKopierOrdererByVersion)
            //avsenderSaksbehandlerident = mapAvsenderSaksbehandler(oppgaveKopierOrdererByVersion)
            //avsenderEnhet = mapAvsenderEnhet(oppgaveKopierOrdererByVersion)
        }

        return Pair(klagebehandlingRepository.findByMottakId(mottak.id)!!, mottak)
    }

    private fun mapHjemler(oppgaveKopiVersjon: OppgaveKopiVersjon) =
        hjemmelService.getHjemmelFromOppgaveKopiVersjon(oppgaveKopiVersjon)

    private fun mapOrganisasjonsnummer(ident: VersjonIdent) =
        if (ident.identType == IdentType.ORGNR) {
            ident.verdi
        } else {
            null
        }

    private fun mapSakstype(behandlingstype: String): Sakstype = Sakstype.of(behandlingstype)

    private fun mapTema(tema: String): Tema = Tema.of(tema)

    //Oppgaven kan ha gått ping-pong frem og tilbake, så det vi leter etter her er siste gang den ble assignet KA
    private fun findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopiVersjoner: List<OppgaveKopiVersjon>): OppgaveKopiVersjon? =
        oppgaveKopiVersjoner.zipWithNext()
            .firstOrNull {
                it.first.tildeltEnhetsnr.startsWith(KLAGEINSTANS_PREFIX)
                        && !it.second.tildeltEnhetsnr.startsWith(KLAGEINSTANS_PREFIX)
            }
            ?.first

    fun assignKlagebehandling(klagebehandlingId: UUID, saksbehandlerIdent: String?): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        klagebehandling.tildeltSaksbehandlerident = saksbehandlerIdent
        klagebehandling.modified = LocalDateTime.now()
        return klagebehandling
    }
}
