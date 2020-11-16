package no.nav.klage.oppgave.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class KlageProxyClientConfiguration(private val webClientBuilder: WebClient.Builder) {

    @Value("\${KLAGEPROXY_URL}")
    private lateinit var url: String

    @Bean
    fun klageProxyWebClient(): WebClient =
        webClientBuilder
            .baseUrl(url)
            .clientConnector(ReactorClientHttpConnector(HttpClient.newConnection()))
            .build()
}
