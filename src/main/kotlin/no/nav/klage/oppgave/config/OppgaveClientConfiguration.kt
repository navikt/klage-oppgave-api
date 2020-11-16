package no.nav.klage.oppgave.config

import no.finn.unleash.Unleash
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient.newConnection

@Configuration
class OppgaveClientConfiguration(private val webClientBuilder: WebClient.Builder, private val unleash: Unleash) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${OPPGAVE_URL_Q1}")
    private lateinit var oppgaveServiceURLQ1: String

    @Value("\${OPPGAVE_URL_Q2}")
    private lateinit var oppgaveServiceURLQ2: String

    @Value("\${OPPGAVE_APIKEY_Q1}")
    private lateinit var apiKeyQ1: String

    @Value("\${OPPGAVE_APIKEY_Q2}")
    private lateinit var apiKeyQ2: String

    @Bean("oppgaveWebClientQ1")
    fun oppgaveWebClientQ1(): WebClient {
        return webClientBuilder
            .defaultHeader("x-nav-apiKey", apiKeyQ2)
            .baseUrl(oppgaveServiceURLQ2)
            .clientConnector(ReactorClientHttpConnector(newConnection()))
            .build()
    }

    @Bean("oppgaveWebClientQ2")
    fun oppgaveWebClientQ2(): WebClient {
        return webClientBuilder
            .defaultHeader("x-nav-apiKey", apiKeyQ1)
            .baseUrl(oppgaveServiceURLQ1)
            .clientConnector(ReactorClientHttpConnector(newConnection()))
            .build()
    }
}