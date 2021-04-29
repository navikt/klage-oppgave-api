package no.nav.klage.oppgave.api.mapper


import no.nav.klage.oppgave.api.view.KlagebehandlingDetaljerView
import no.nav.klage.oppgave.api.view.KlagebehandlingListView
import no.nav.klage.oppgave.api.view.KvalitetsvurderingView
import no.nav.klage.oppgave.api.view.VedtakView
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.norg2.Norg2Client
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.clients.saf.rest.ArkivertDokument
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.PartId
import no.nav.klage.oppgave.domain.klage.PartIdType
import no.nav.klage.oppgave.domain.klage.Vedtak
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class KlagebehandlingMapper(
    private val pdlFacade: PdlFacade,
    private val egenAnsattService: EgenAnsattService,
    private val norg2Client: Norg2Client
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapKlagebehandlingOgMottakToEsKlagebehandling(klagebehandling: Klagebehandling): EsKlagebehandling {
        val foedselsnummer = foedselsnummer(klagebehandling.klager.partId)

        val personInfo = foedselsnummer?.let { pdlFacade.getPersonInfo(it) }
        val erFortrolig = personInfo?.harBeskyttelsesbehovFortrolig() ?: false
        val erStrengtFortrolig = personInfo?.harBeskyttelsesbehovStrengtFortrolig() ?: false
        val erEgenAnsatt = foedselsnummer?.let { egenAnsattService.erEgenAnsatt(it) } ?: false
        val navn = personInfo?.navn

        return EsKlagebehandling(
            id = klagebehandling.id.toString(),
            versjon = klagebehandling.versjon,
            journalpostId = klagebehandling.saksdokumenter.map { it.journalpostId },
            saksreferanse = klagebehandling.kildeReferanse,
            tildeltEnhet = klagebehandling.tildeltEnhet,
            tema = klagebehandling.tema.id,
            type = klagebehandling.type.id,
            tildeltSaksbehandlerident = klagebehandling.tildeltSaksbehandlerident,
            medunderskriverident = klagebehandling.medunderskriverident,
            innsendt = klagebehandling.innsendt,
            mottattFoersteinstans = klagebehandling.mottattFoersteinstans,
            mottattKlageinstans = klagebehandling.mottattKlageinstans,
            frist = klagebehandling.frist,
            startet = klagebehandling.startet,
            avsluttet = klagebehandling.avsluttet,
            hjemler = klagebehandling.hjemler.map { it.id },
            foedselsnummer = foedselsnummer,
            virksomhetsnummer = virksomhetsnummer(klagebehandling.klager.partId),
            navn = navn,
            egenAnsatt = erEgenAnsatt,
            fortrolig = erFortrolig,
            strengtFortrolig = erStrengtFortrolig
        )
    }

    fun mapEsKlagebehandlingerToListView(
        esKlagebehandlinger: List<EsKlagebehandling>,
        fetchPersoner: Boolean,
        saksbehandler: String?
    ): List<KlagebehandlingListView> {
        return esKlagebehandlinger.map { esKlagebehandling ->
            KlagebehandlingListView(
                id = esKlagebehandling.id,
                person = if (fetchPersoner) {
                    KlagebehandlingListView.Person(
                        esKlagebehandling.foedselsnummer,
                        esKlagebehandling.navn
                    )
                } else {
                    null
                },
                type = esKlagebehandling.type,
                tema = esKlagebehandling.tema,
                hjemmel = esKlagebehandling.hjemler?.firstOrNull(),
                frist = esKlagebehandling.frist,
                mottatt = esKlagebehandling.mottattKlageinstans,
                versjon = esKlagebehandling.versjon!!.toInt(),
                klagebehandlingVersjon = esKlagebehandling.versjon,
                erMedunderskriver = esKlagebehandling.medunderskriverident?.equals(saksbehandler) ?: false
            )
        }
    }

    fun mapKlagebehandlingToKlagebehandlingDetaljerView(klagebehandling: Klagebehandling): KlagebehandlingDetaljerView {
        val enhetNavn = klagebehandling.avsenderEnhetFoersteinstans?.let { norg2Client.fetchEnhet(it) }?.navn
        val sakenGjelderFoedselsnummer = foedselsnummer(klagebehandling.sakenGjelder.partId)
        val sakenGjelder = sakenGjelderFoedselsnummer?.let { pdlFacade.getPersonInfo(it) }

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
            sakenGjelderVirksomhetsnummer = virksomhetsnummer(klagebehandling.sakenGjelder.partId),
            foedselsnummer = foedselsnummer(klagebehandling.klager.partId),
            virksomhetsnummer = virksomhetsnummer(klagebehandling.klager.partId),
            tema = klagebehandling.tema.id,
            type = klagebehandling.type.id,
            mottatt = klagebehandling.mottattKlageinstans,
            startet = klagebehandling.startet,
            avsluttet = klagebehandling.avsluttet,
            frist = klagebehandling.frist,
            tildeltSaksbehandlerident = klagebehandling.tildeltSaksbehandlerident,
            medunderskriverident = klagebehandling.medunderskriverident,
            hjemler = klagebehandling.hjemler.map { it.id },
            modified = klagebehandling.modified,
            created = klagebehandling.created,
            grunn = klagebehandling.kvalitetsvurdering?.grunn?.id,
            eoes = klagebehandling.kvalitetsvurdering?.eoes?.id,
            raadfoertMedLege = klagebehandling.kvalitetsvurdering?.raadfoertMedLege?.id,
            internVurdering = klagebehandling.kvalitetsvurdering?.internVurdering,
            sendTilbakemelding = klagebehandling.kvalitetsvurdering?.sendTilbakemelding,
            tilbakemelding = klagebehandling.kvalitetsvurdering?.tilbakemelding,
            klagebehandlingVersjon = klagebehandling.versjon,
            vedtak = klagebehandling.vedtak.map { mapVedtakToVedtakView(it) },
            kommentarFraFoersteinstans = klagebehandling.kommentarFraFoersteinstans
        )
    }

    fun mapVedtakToVedtakView(vedtak: Vedtak, dokument: ArkivertDokument? = null): VedtakView {
        if (dokument != null) {
            return VedtakView(
                id = vedtak.id,
                utfall = vedtak.utfall,
                hjemler = vedtak.hjemler,
                brevMottakere = vedtak.brevmottakere,
                finalized = vedtak.finalized,
                content = Base64.getEncoder().encodeToString(dokument.bytes)
            )
        } else {
            return VedtakView(
                id = vedtak.id,
                utfall = vedtak.utfall,
                hjemler = vedtak.hjemler,
                brevMottakere = vedtak.brevmottakere,
                finalized = vedtak.finalized
            )
        }
    }


    fun mapKlagebehandlingToKvalitetsvurderingView(klagebehandling: Klagebehandling): KvalitetsvurderingView {
        val kvalitetsvurdering = klagebehandling.kvalitetsvurdering
        return KvalitetsvurderingView(
            grunn = kvalitetsvurdering?.grunn?.id,
            eoes = kvalitetsvurdering?.eoes?.id,
            raadfoertMedLege = kvalitetsvurdering?.raadfoertMedLege?.id,
            internVurdering = kvalitetsvurdering?.internVurdering,
            sendTilbakemelding = kvalitetsvurdering?.sendTilbakemelding,
            tilbakemelding = kvalitetsvurdering?.tilbakemelding,
            klagebehandlingVersjon = klagebehandling.versjon
        )
    }

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

