package no.nav.klage.oppgave.clients

import brave.Tracer
import no.nav.klage.oppgave.domain.Bruker
import no.nav.klage.oppgave.domain.Tilganger
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class AxsysClient(private val axsysWebClient: WebClient, private val tracer: Tracer) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${spring.application.name}")
    lateinit var applicationName: String

    @Retryable
    //TODO: Ta i bruk @Cacheable
    fun getTilgangerForSaksbehandler(navIdent: String): Tilganger {
        logger.debug("Fetching tilganger for saksbehandler with Nav-Ident {}", navIdent)

        return try {
            axsysWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/tilgang/{navIdent}")
                        .queryParam("inkluderAlleEnheter", "true")
                        .build(navIdent)
                }.header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
                .header("Nav-Consumer-Id", applicationName)

                .retrieve()
                .bodyToMono<Tilganger>()
                .block() ?: throw RuntimeException("Tilganger could not be fetched")
        } catch (notFound: WebClientResponseException.NotFound) {
            logger.warn("Got a 404 fetching tilganger for saksbehandler {}, returning empty object", navIdent)
            //TODO: Burde det smelle hardt her isf å returnere tomt objekt?
            Tilganger(emptyArray())
        }
    }

    @Retryable
    //TODO: Ta i bruk @Cacheable
    fun getSaksbehandlereIEnhet(enhetId: String): List<Bruker> {
        logger.debug("Fetching brukere in enhet {}", enhetId)

        return axsysWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/enhet/{enhetId}/brukere")
                    .build(enhetId)
            }.header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
            .header("Nav-Consumer-Id", applicationName)

            .retrieve()
            .bodyToMono<List<Bruker>>()
            .block() ?: throw RuntimeException("Brukere in enhet could not be fetched")
    }
}



