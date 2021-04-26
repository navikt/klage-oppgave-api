package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.function.Consumer


@Configuration
class JoarkClientConfiguration(
    private val webClientBuilder: WebClient.Builder
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${JOARK_SERVICE_URL}")
    private lateinit var joarkServiceURL: String

    @Value("\${JOURNALPOST_APIKEY}")
    private lateinit var apiKey: String

    @Bean
    fun joarkWebClient(): WebClient {
        return webClientBuilder
            .defaultHeader("x-nav-apiKey", apiKey)
            .baseUrl(joarkServiceURL)
            .filter(logRequest())
            .build()
    }

    private fun logRequest(): ExchangeFilterFunction? {
        return ExchangeFilterFunction.ofRequestProcessor { clientRequest: ClientRequest ->
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url())
            clientRequest.headers()
                .forEach { name: String?, values: List<String?> ->
                    values.forEach(
                        Consumer { value: String? ->
                            logger.info(
                                "{}={}",
                                name,
                                value
                            )
                        })
                }
            Mono.just(clientRequest)
        }
    }
}