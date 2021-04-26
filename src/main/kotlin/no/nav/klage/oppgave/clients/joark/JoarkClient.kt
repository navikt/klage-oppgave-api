package no.nav.klage.oppgave.clients.joark

import brave.Tracer
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.sts.StsClient
import no.nav.klage.oppgave.domain.joark.*
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.PartIdType
import no.nav.klage.oppgave.service.TokenService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.apache.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Component
class JoarkClient(
    private val joarkWebClient: WebClient,
    private val stsClient: StsClient,
    private val tokenService: TokenService,
    private val tracer: Tracer,
    private val pdlFacade: PdlFacade
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private const val BREV_TITTEL = "Brev fra Klageinstans"
        private const val BREVKODE = "BREV_FRA_KLAGEINSTANS"
        private const val BEHANDLINGSTEMA_KLAGE_KLAGEINSTANS = "ab0164"
        private const val JOURNALFOERENDE_ENHET = "4291"
    }

    fun createJournalpost(klagebehandling: Klagebehandling, uploadedDocument: ByteArray? = null, forsoekFerdigstill: Boolean? = false, fagsak: Boolean? = false): String {

        val journalpost = this.createJournalpostObject(klagebehandling, uploadedDocument, fagsak)

        val journalpostResponse = joarkWebClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .queryParam("forsoekFerdigstill", forsoekFerdigstill)
                    .build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenService.getStsSystembrukerToken()}")
//            .header("Nav-Consumer-Token", "Bearer ${tokenService.getSaksbehandlerAccessTokenWithGraphScope()}")
            .header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(journalpost)
            .retrieve()
            .bodyToMono(JournalpostResponse::class.java)
            .block()
            ?: throw RuntimeException("Journalpost could not be created for klagebehandling with id ${klagebehandling.id}.")

        logger.debug("Journalpost successfully created in Joark with id {}.", journalpostResponse.journalpostId)

        return journalpostResponse.journalpostId
    }

    fun finalizeJournalpost(journalpostId: String): String {
        val response = joarkWebClient.patch()
            .uri ("/${journalpostId}/ferdigstill")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenService.getStsSystembrukerToken()}")
//            .header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(FerdigstillJournalpostPayload(JOURNALFOERENDE_ENHET))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
            ?: throw RuntimeException("Journalpost with id $journalpostId could not be finalized.")

        logger.debug("Journalpost with id $journalpostId was succesfully finalized.")

        return response
    }

    private fun createJournalpostObject(klagebehandling: Klagebehandling, uploadedDocument: ByteArray?, fagsak: Boolean? = false): Journalpost =
        Journalpost(
            journalposttype = JournalpostType.UTGAAENDE,
            tema = klagebehandling.tema,
            behandlingstema = BEHANDLINGSTEMA_KLAGE_KLAGEINSTANS,
            avsenderMottaker = createAvsenderMottager(klagebehandling),
            sak = createSak(klagebehandling, fagsak),
            tittel = BREV_TITTEL,
            journalfoerendeEnhet = JOURNALFOERENDE_ENHET,
            eksternReferanseId = tracer.currentSpan().context().traceIdString(),
            bruker = createBruker(klagebehandling),
            dokumenter = createDokument(uploadedDocument)
        )



    private fun createAvsenderMottager(klagebehandling: Klagebehandling): AvsenderMottaker? {
        val klager = klagebehandling.klager
        if (klager.partId.type.equals(PartIdType.PERSON)) {
            val person = pdlFacade.getPersonInfo(klager.partId.value)
            return person.let {
                AvsenderMottaker(
                    id = it.foedselsnr,
                    idType = AvsenderMottakerIdType.FNR,
                    navn = it.settSammenNavn()
                )
            }
        } else {
            return AvsenderMottaker(
                klager.partId.value,
                AvsenderMottakerIdType.ORGNR,
                //TODO: Vent på ereg-integrasjon, er på vei ellers i løsningen
                navn = "Navn Navnesen AS"
            )
        }
    }

    private fun createSak(klagebehandling: Klagebehandling, fagsak: Boolean? = false): Sak? {
        //Finn ut mer her
        return if (fagsak!!) Sak(Sakstype.FAGSAK, FagsaksSystem.K9, "12345") else
            Sak(Sakstype.GENERELL_SAK)

    }

    private fun createBruker(klagebehandling: Klagebehandling): Bruker? {
        return klagebehandling.klager.partId.let {
            Bruker(
                it.value,
                BrukerIdType.FNR
            )
        }
    }


    private fun createDokument(uploadedDocument: ByteArray?): List<Dokument> {
        val hovedDokument = Dokument(
            tittel = BREV_TITTEL,
            brevkode = BREVKODE,
            dokumentVarianter = listOf(
                DokumentVariant(
                    filtype = "PDFA",
                    variantformat = "ARKIV",
                    fysiskDokument = Base64.getEncoder().encodeToString(uploadedDocument)
                )
            ),

        )
        return listOf(hovedDokument)
    }
}