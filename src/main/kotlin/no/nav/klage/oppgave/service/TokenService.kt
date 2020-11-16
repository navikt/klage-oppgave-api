package no.nav.klage.oppgave.service

import no.finn.unleash.Unleash
import no.nav.klage.oppgave.clients.StsClient
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val clientConfigurationProperties: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
    private val tokenValidationContextHolder: TokenValidationContextHolder,
    private val stsClient: StsClient,
    private val unleash: Unleash
) {

    fun getSaksbehandlerAccessTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["azure-onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    fun getFeatureToggledAccessTokenForOppgave(): String {
        return if (unleash.isEnabled("OppgaveMedBrukerkontekst")) {
            val clientProperties = clientConfigurationProperties.registration["oppgave-onbehalfof"]
            val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
            response.accessToken
        } else {
            getStsSystembrukerToken()
        }
    }

    fun getAppAccessTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["app"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    fun getStsSystembrukerToken(): String = stsClient.oidcToken()

    fun getAccessTokenFrontendSent(): String =
        tokenValidationContextHolder.tokenValidationContext.getJwtToken(SecurityConfiguration.ISSUER_AAD).tokenAsString

}