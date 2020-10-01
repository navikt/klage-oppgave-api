package no.nav.klage.oppgave.clients

import brave.Tracer
import no.nav.klage.oppgave.domain.Tilganger
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class AxsysClient(private val axsysWebClient: WebClient, private val tracer: Tracer) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${spring.application.name}")
    lateinit var applicationName: String

    fun getTilgangerForSaksbehandler(navIdent: String): Tilganger {
        logger.debug("Fetching tilganger for saksbehandler with Nav-Ident {}", navIdent)

        return axsysWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/{navIdent}")
                    .queryParam("inkluderAlleEnheter", "true")
                    .build(navIdent)
            }.header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
            .header("Nav-Consumer-Id", applicationName)

            .retrieve()
            .bodyToMono<Tilganger>()
            .block() ?: throw RuntimeException("Tilganger could not be fetched")
    }
}



