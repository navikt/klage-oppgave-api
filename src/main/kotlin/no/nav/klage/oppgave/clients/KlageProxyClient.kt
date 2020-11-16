package no.nav.klage.oppgave.clients

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.http.HttpHeaders
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KlageProxyClient(
    private val klageProxyWebClient: WebClient,
    private val clientConfigurationProperties: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService
) {

    @Retryable
    //TODO: Ta i bruk @Cacheable
    fun getRoller(ident: String): List<String> {
        return klageProxyWebClient.get()
            .uri("/roller/{ident}", ident)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${getSaksbehandlersTokenWithProxyScope()}")
            .retrieve()
            .bodyToMono<List<String>>()
            .block()
            ?: throw RuntimeException("Unable to get roller for $ident")
    }

    private fun getSaksbehandlersTokenWithProxyScope(): String {
        val clientProperties = clientConfigurationProperties.registration["proxy-onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }
}
