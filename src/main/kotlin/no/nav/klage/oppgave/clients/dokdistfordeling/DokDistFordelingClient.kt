package no.nav.klage.oppgave.clients.dokdistfordeling

import brave.Tracer
import no.nav.klage.oppgave.service.TokenService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.apache.http.HttpHeaders
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class DokDistFordelingClient(
    private val dokDistWebClient: WebClient,
    private val tracer: Tracer,
    private val tokenService: TokenService
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Value("\${spring.application.name}")
    lateinit var applicationName: String

    fun distribuerJournalpost(journalpostId: String): DistribuerJournalpostResponse {
        val payload = DistribuerJournalpostRequestTo(
            journalpostId = journalpostId,
            bestillendeFagSystem = applicationName,
            dokumentProdApp = applicationName
        )
        val distribuerJournalpostResponse = dokDistWebClient.post()
            .header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
            .header("Nav-Consumer-Id", applicationName)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenService.getSaksbehandlerAccessTokenWithSafScope()}")
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(DistribuerJournalpostResponse::class.java)
            .block()
            ?: throw RuntimeException("Journalpost with id $journalpostId could not be distributed.")

        logger.debug(
            "Journalpost with id {} successfully distributed, resulting in bestillingsId {}.",
            journalpostId,
            distribuerJournalpostResponse.bestillingsId
        )

        return distribuerJournalpostResponse
    }
}