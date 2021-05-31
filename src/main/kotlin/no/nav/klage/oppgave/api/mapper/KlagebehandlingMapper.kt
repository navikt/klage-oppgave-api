package no.nav.klage.oppgave.api.mapper


import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.ereg.EregClient
import no.nav.klage.oppgave.clients.norg2.Norg2Client
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.clients.saf.rest.ArkivertDokument
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.elasticsearch.EsSaksdokument
import no.nav.klage.oppgave.domain.elasticsearch.EsVedtak
import no.nav.klage.oppgave.domain.klage.BrevMottaker
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.PartId
import no.nav.klage.oppgave.domain.klage.Vedtak
import no.nav.klage.oppgave.domain.kodeverk.PartIdType
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.VedtakService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class KlagebehandlingMapper(
    private val pdlFacade: PdlFacade,
    private val egenAnsattService: EgenAnsattService,
    private val norg2Client: Norg2Client,
    private val eregClient: EregClient,
    private val vedtakService: VedtakService,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapKlagebehandlingOgMottakToEsKlagebehandling(klagebehandling: Klagebehandling): EsKlagebehandling {
        val klagerFnr = foedselsnummer(klagebehandling.klager.partId)
        val klagerPersonInfo = klagerFnr?.let { pdlFacade.getPersonInfo(it) }

        val klagerOrgnr = virksomhetsnummer(klagebehandling.klager.partId)
        val klagerOrgnavn = klagerOrgnr?.let { eregClient.hentOrganisasjon(it)?.navn?.sammensattNavn() }

        val sakenGjelderFnr = foedselsnummer(klagebehandling.sakenGjelder.partId)
        val sakenGjelderPersonInfo = sakenGjelderFnr?.let { pdlFacade.getPersonInfo(it) }

        val sakenGjelderOrgnr = virksomhetsnummer(klagebehandling.sakenGjelder.partId)
        val sakenGjelderOrgnavn = sakenGjelderOrgnr?.let { eregClient.hentOrganisasjon(it)?.navn?.sammensattNavn() }


        val erFortrolig = sakenGjelderPersonInfo?.harBeskyttelsesbehovFortrolig() ?: false
        val erStrengtFortrolig = sakenGjelderPersonInfo?.harBeskyttelsesbehovStrengtFortrolig() ?: false
        val erEgenAnsatt = sakenGjelderFnr?.let { egenAnsattService.erEgenAnsatt(it) } ?: false

        return EsKlagebehandling(
            id = klagebehandling.id.toString(),
            versjon = klagebehandling.versjon,
            klagerFnr = klagerFnr,
            klagerNavn = klagerPersonInfo?.sammensattNavn,
            klagerFornavn = klagerPersonInfo?.fornavn,
            klagerMellomnavn = klagerPersonInfo?.mellomnavn,
            klagerEtternavn = klagerPersonInfo?.etternavn,
            klagerOrgnr = klagerOrgnr,
            klagerOrgnavn = klagerOrgnavn,
            sakenGjelderFnr = sakenGjelderFnr,
            sakenGjelderNavn = sakenGjelderPersonInfo?.sammensattNavn,
            sakenGjelderFornavn = sakenGjelderPersonInfo?.fornavn,
            sakenGjelderMellomnavn = sakenGjelderPersonInfo?.mellomnavn,
            sakenGjelderEtternavn = sakenGjelderPersonInfo?.etternavn,
            sakenGjelderOrgnr = sakenGjelderOrgnr,
            sakenGjelderOrgnavn = sakenGjelderOrgnavn,
            tema = klagebehandling.tema.id,
            type = klagebehandling.type.id,
            kildeReferanse = klagebehandling.kildeReferanse,
            sakFagsystem = klagebehandling.sakFagsystem?.id,
            sakFagsakId = klagebehandling.sakFagsakId,
            innsendt = klagebehandling.innsendt,
            mottattFoersteinstans = klagebehandling.mottattFoersteinstans,
            avsenderSaksbehandleridentFoersteinstans = klagebehandling.avsenderSaksbehandleridentFoersteinstans,
            avsenderEnhetFoersteinstans = klagebehandling.avsenderEnhetFoersteinstans,
            mottattKlageinstans = klagebehandling.mottattKlageinstans,
            tildelt = klagebehandling.tildeling?.tidspunkt,
            foerstTildelt = klagebehandling.tildelingHistorikk.map { it.tildeling.tidspunkt }.minOrNull(),
            sistTildelt = klagebehandling.tildeling?.tidspunkt,
            avsluttet = klagebehandling.avsluttet,
            avsluttetAvSaksbehandler = klagebehandling.avsluttetAvSaksbehandler,
            frist = klagebehandling.frist,
            tildeltSaksbehandlerident = klagebehandling.tildeling?.saksbehandlerident,
            medunderskriverident = klagebehandling.medunderskriver?.saksbehandlerident,
            sendtMedunderskriver = klagebehandling.medunderskriver?.tidspunkt,
            tildeltEnhet = klagebehandling.tildeling?.enhet,
            hjemler = klagebehandling.hjemler.map { it.id },
            created = klagebehandling.created,
            modified = klagebehandling.modified,
            kilde = klagebehandling.kildesystem.id,
            kommentarFraFoersteinstans = klagebehandling.kommentarFraFoersteinstans,
            internVurdering = klagebehandling.kvalitetsvurdering?.internVurdering,
            vedtak = klagebehandling.vedtak.map { vedtak ->
                EsVedtak(
                    utfall = vedtak.utfall?.id,
                    grunn = vedtak.grunn?.id,
                    hjemler = vedtak.hjemler.map { hjemmel -> hjemmel.id },
                    brevmottakerFnr = vedtak.brevmottakere.filter { it.partId.type == PartIdType.PERSON }
                        .map { it.partId.value },
                    brevmottakerOrgnr = vedtak.brevmottakere.filter { it.partId.type == PartIdType.VIRKSOMHET }
                        .map { it.partId.value },
                    journalpostId = vedtak.journalpostId,
                    created = vedtak.created,
                    modified = vedtak.modified,
                    ferdigstiltIJoark = vedtak.ferdigstiltIJoark
                )
            },
            saksdokumenter = klagebehandling.saksdokumenter.map { EsSaksdokument(it.journalpostId, it.dokumentInfoId) },
            saksdokumenterJournalpostId = klagebehandling.saksdokumenter.map { it.journalpostId },
            saksdokumenterJournalpostIdOgDokumentInfoId = klagebehandling.saksdokumenter.map {
                it.journalpostId + it.dokumentInfoId
            },
            egenAnsatt = erEgenAnsatt,
            fortrolig = erFortrolig,
            strengtFortrolig = erStrengtFortrolig,
            vedtakUtfall = klagebehandling.vedtak.firstOrNull()?.utfall?.id,
            vedtakGrunn = klagebehandling.vedtak.firstOrNull()?.grunn?.id,
            vedtakHjemler = klagebehandling.vedtak.firstOrNull()?.hjemler?.map { hjemmel -> hjemmel.id } ?: emptyList(),
            vedtakBrevmottakerFnr = klagebehandling.vedtak.firstOrNull()?.brevmottakere?.filter { it.partId.type == PartIdType.PERSON }
                ?.map { it.partId.value } ?: emptyList(),
            vedtakBrevmottakerOrgnr = klagebehandling.vedtak.firstOrNull()?.brevmottakere?.filter { it.partId.type == PartIdType.VIRKSOMHET }
                ?.map { it.partId.value } ?: emptyList(),
            vedtakJournalpostId = klagebehandling.vedtak.firstOrNull()?.journalpostId,
            vedtakCreated = klagebehandling.vedtak.firstOrNull()?.created,
            vedtakModified = klagebehandling.vedtak.firstOrNull()?.modified,
            vedtakFerdigstiltIJoark = klagebehandling.vedtak.firstOrNull()?.ferdigstiltIJoark,
            temaNavn = klagebehandling.tema.name,
            typeNavn = klagebehandling.type.name,
            hjemlerNavn = klagebehandling.hjemler.map { it.name },
            vedtakUtfallNavn = klagebehandling.vedtak.firstOrNull()?.utfall?.name,
            vedtakGrunnNavn = klagebehandling.vedtak.firstOrNull()?.grunn?.name,
            sakFagsystemNavn = klagebehandling.sakFagsystem?.name,
            status = EsKlagebehandling.Status.valueOf(klagebehandling.getStatus().name)
        )
    }

    fun mapEsKlagebehandlingerToListView(
        esKlagebehandlinger: List<EsKlagebehandling>,
        viseUtvidet: Boolean,
        viseFullfoerte: Boolean,
        saksbehandler: String?
    ): List<KlagebehandlingListView> {
        return esKlagebehandlinger.map { esKlagebehandling ->
            KlagebehandlingListView(
                id = esKlagebehandling.id,
                person = if (viseUtvidet) {
                    KlagebehandlingListView.Person(
                        esKlagebehandling.sakenGjelderFnr,
                        esKlagebehandling.sakenGjelderNavn
                    )
                } else {
                    null
                },
                type = esKlagebehandling.type,
                tema = esKlagebehandling.tema,
                hjemmel = esKlagebehandling.hjemler.firstOrNull(),
                frist = esKlagebehandling.frist,
                mottatt = esKlagebehandling.mottattKlageinstans?.toLocalDate(),
                versjon = esKlagebehandling.versjon!!.toInt(),
                klagebehandlingVersjon = esKlagebehandling.versjon,
                erMedunderskriver = if (esKlagebehandling.medunderskriverident != null) {
                    esKlagebehandling.medunderskriverident == saksbehandler
                } else null,
                medunderskriverident = esKlagebehandling.medunderskriverident,
                utfall = if (viseFullfoerte) {
                    esKlagebehandling.vedtakUtfall
                } else {
                    null
                },
                //TODO: Skal fjernes eller få ny betydning når frontend tatt i bruk avsluttetAvSaksbehandler.
                avsluttet = if (viseFullfoerte) {
                    esKlagebehandling.avsluttetAvSaksbehandler?.toLocalDate()
                } else {
                    null
                },
                avsluttetAvSaksbehandler = if (viseFullfoerte) {
                    esKlagebehandling.avsluttetAvSaksbehandler?.toLocalDate()
                } else {
                    null
                }
            )
        }
    }

    fun mapKlagebehandlingToKlagebehandlingDetaljerView(klagebehandling: Klagebehandling): KlagebehandlingDetaljerView {
        val enhetNavn = klagebehandling.avsenderEnhetFoersteinstans?.let { norg2Client.fetchEnhet(it) }?.navn
        val sakenGjelderFoedselsnummer = foedselsnummer(klagebehandling.sakenGjelder.partId)
        val sakenGjelder = sakenGjelderFoedselsnummer?.let { pdlFacade.getPersonInfo(it) }
        val sakenGjelderVirksomhetsnummer = virksomhetsnummer(klagebehandling.sakenGjelder.partId)
        val sakenGjelderVirksomhet = sakenGjelderVirksomhetsnummer?.let { eregClient.hentOrganisasjon(it) }
        val klagerFoedselsnummer = foedselsnummer(klagebehandling.klager.partId)
        val klager = klagerFoedselsnummer?.let { pdlFacade.getPersonInfo(it) }
        val klagerVirksomhetsnummer = virksomhetsnummer(klagebehandling.klager.partId)
        val klagerVirksomhet = klagerVirksomhetsnummer?.let { eregClient.hentOrganisasjon(it) }

        return KlagebehandlingDetaljerView(
            id = klagebehandling.id,
            klageInnsendtdato = klagebehandling.innsendt,
            fraNAVEnhet = klagebehandling.avsenderEnhetFoersteinstans,
            fraNAVEnhetNavn = enhetNavn,
            fraSaksbehandlerident = klagebehandling.avsenderSaksbehandleridentFoersteinstans,
            mottattFoersteinstans = klagebehandling.mottattFoersteinstans,
            sakenGjelderFoedselsnummer = sakenGjelderFoedselsnummer,
            sakenGjelderNavn = sakenGjelder.getNavn(),
            sakenGjelderKjoenn = sakenGjelder?.kjoenn,
            sakenGjelderVirksomhetsnummer = sakenGjelderVirksomhetsnummer,
            sakenGjelderVirksomhetsnavn = sakenGjelderVirksomhet?.navn?.sammensattNavn(),
            foedselsnummer = klagerFoedselsnummer, // TODO deprecate
            virksomhetsnummer = klagerVirksomhetsnummer, // TODO deprecate
            klagerFoedselsnummer = klagerFoedselsnummer,
            klagerVirksomhetsnummer = klagerVirksomhetsnummer,
            klagerVirksomhetsnavn = klagerVirksomhet?.navn?.sammensattNavn(),
            klagerNavn = klager.getNavn(),
            klagerKjoenn = klager?.kjoenn,
            tema = klagebehandling.tema.id,
            type = klagebehandling.type.id,
            mottatt = klagebehandling.mottattKlageinstans.toLocalDate(),
            mottattKlageinstans = klagebehandling.mottattKlageinstans.toLocalDate(),
            tildelt = klagebehandling.tildeling?.tidspunkt?.toLocalDate(),
            //TODO: Skal fjernes eller få ny betydning når frontend tatt i bruk avsluttetAvSaksbehandler.
            avsluttet = klagebehandling.avsluttetAvSaksbehandler?.toLocalDate(),
            avsluttetAvSaksbehandler = klagebehandling.avsluttetAvSaksbehandler?.toLocalDate(),
            frist = klagebehandling.frist,
            tildeltSaksbehandlerident = klagebehandling.tildeling?.saksbehandlerident,
            medunderskriverident = klagebehandling.medunderskriver?.saksbehandlerident,
            datoSendtMedunderskriver = klagebehandling.medunderskriver?.tidspunkt?.toLocalDate(),
            hjemler = klagebehandling.hjemler.map { it.id },
            modified = klagebehandling.modified,
            created = klagebehandling.created,
            eoes = klagebehandling.kvalitetsvurdering?.eoes?.id,
            raadfoertMedLege = klagebehandling.kvalitetsvurdering?.raadfoertMedLege?.id,
            internVurdering = klagebehandling.kvalitetsvurdering?.internVurdering,
            sendTilbakemelding = klagebehandling.kvalitetsvurdering?.sendTilbakemelding,
            tilbakemelding = klagebehandling.kvalitetsvurdering?.tilbakemelding,
            klagebehandlingVersjon = klagebehandling.versjon,
            vedtak = klagebehandling.vedtak.map { mapVedtakToVedtakView(it, vedtakService.getVedleggView(
                it,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )) },
            kommentarFraFoersteinstans = klagebehandling.kommentarFraFoersteinstans
        )
    }

    fun mapVedtakToVedtakView(vedtak: Vedtak, vedleggView: VedleggView? = null): VedtakView {
        if (vedleggView != null) {
            return VedtakView(
                id = vedtak.id,
                utfall = vedtak.utfall?.id,
                grunn = vedtak.grunn?.id,
                hjemler = vedtak.hjemler.map { it.id }.toSet(),
                brevMottakere = vedtak.brevmottakere.map { mapBrevmottaker(it) }.toSet(),
                file = vedleggView,
                ferdigstilt = vedtak.ferdigstiltIJoark
            )
        } else {
            return VedtakView(
                id = vedtak.id,
                utfall = vedtak.utfall?.id,
                grunn = vedtak.grunn?.id,
                hjemler = vedtak.hjemler.map { it.id }.toSet(),
                brevMottakere = vedtak.brevmottakere.map { mapBrevmottaker(it) }.toSet(),
                ferdigstilt = vedtak.ferdigstiltIJoark
            )
        }
    }

    fun mapArkivertDokumentToVedleggView(arkivertDokument: ArkivertDokument, dokumentName: String): VedleggView {
        return VedleggView(
            dokumentName,
            arkivertDokument.bytes.size.toLong(),
            Base64.getEncoder().encodeToString(arkivertDokument.bytes)
        )
    }

    private fun mapBrevmottaker(it: BrevMottaker) = BrevMottakerView(
        it.partId.type.id,
        it.partId.value,
        it.rolle.id
    )

    private fun foedselsnummer(partId: PartId) =
        if (partId.type == PartIdType.PERSON) {
            partId.value
        } else {
            null
        }

    private fun virksomhetsnummer(partId: PartId) =
        if (partId.type == PartIdType.VIRKSOMHET) {
            partId.value
        } else {
            null
        }

    private fun Person?.getNavn(): KlagebehandlingDetaljerView.Navn? =
        if (this != null) {
            KlagebehandlingDetaljerView.Navn(
                fornavn = fornavn,
                mellomnavn = mellomnavn,
                etternavn = etternavn
            )
        } else {
            null
        }

}

