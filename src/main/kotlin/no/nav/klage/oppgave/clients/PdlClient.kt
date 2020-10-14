package no.nav.klage.oppgave.clients

import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.pdl.HentPersonResponse
import no.nav.klage.oppgave.domain.pdl.hentPersonQuery
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class PdlClient(
    private val pdlWebClient: WebClient,
    val tokenValidationContextHolder: TokenValidationContextHolder,
    private val stsClient: StsClient
) {

    fun getPersonInfo(fnrList: List<String>): HentPersonResponse {
        return pdlWebClient.post()
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenValidationContextHolder.tokenValidationContext.getJwtToken(ISSUER_AAD).tokenAsString}"
            )
            .header("Nav-Consumer-Token", "Bearer ${stsClient.oidcToken()}")
            .bodyValue(hentPersonQuery(fnrList))
            .retrieve()
            .bodyToMono<HentPersonResponse>()
            .block() ?: throw RuntimeException("Person not found")
    }
}
