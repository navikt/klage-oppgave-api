package no.nav.klage.oppgave.clients.fssproxy

import no.nav.klage.oppgave.config.CacheWithRedisConfiguration.Companion.ROLLER_CACHE
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.cache.annotation.Cacheable
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
    @Cacheable(ROLLER_CACHE)
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
