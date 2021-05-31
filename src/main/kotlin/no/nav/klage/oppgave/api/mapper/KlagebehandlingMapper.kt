package no.nav.klage.oppgave.api.mapper


import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.ereg.EregClient
import no.nav.klage.oppgave.clients.norg2.Norg2Client
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.domain.ArkivertDokumentWithTitle
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.klage.BrevMottaker
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.PartId
import no.nav.klage.oppgave.domain.klage.Vedtak
import no.nav.klage.oppgave.domain.kodeverk.PartIdType
import no.nav.klage.oppgave.service.DokumentService
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
    private val dokumentService: DokumentService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
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
                harMedunderskriver = esKlagebehandling.medunderskriverident != null,
                erMedunderskriver = esKlagebehandling.medunderskriverident != null && esKlagebehandling.medunderskriverident == saksbehandler,
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
            vedtak = klagebehandling.vedtak.map { mapVedtakToVedtakView(it) },
            kommentarFraFoersteinstans = klagebehandling.kommentarFraFoersteinstans
        )
    }

    fun mapVedtakToVedtakView(vedtak: Vedtak): VedtakView {
        return VedtakView(
            id = vedtak.id,
            utfall = vedtak.utfall?.id,
            grunn = vedtak.grunn?.id,
            hjemler = vedtak.hjemler.map { it.id }.toSet(),
            brevMottakere = vedtak.brevmottakere.map { mapBrevmottaker(it) }.toSet(),
            file = getVedleggView(vedtak.journalpostId),
            ferdigstilt = vedtak.ferdigstiltIJoark
        )
    }

    fun getVedleggView(vedtakJournalpostId: String?): VedleggView? {
        return vedtakJournalpostId?.let {
            val arkivertDokumentWithTitle = dokumentService.getArkivertDokumentWithTitle(it)
            mapArkivertDokumentWithTitleToVedleggView(arkivertDokumentWithTitle)
        }
    }

    fun mapArkivertDokumentWithTitleToVedleggView(arkivertDokumentWithTitle: ArkivertDokumentWithTitle): VedleggView {
        return VedleggView(
            arkivertDokumentWithTitle.title,
            arkivertDokumentWithTitle.content.size.toLong(),
            Base64.getEncoder().encodeToString(arkivertDokumentWithTitle.content)
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

