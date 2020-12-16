package no.nav.klage.oppgave.clients.pdl

import no.nav.klage.oppgave.service.TokenService
import org.springframework.http.HttpHeaders
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class PdlClient(
    private val pdlWebClient: WebClient,
    private val tokenService: TokenService
) {

    @Retryable
    fun getPersonInfo(fnrList: List<String>): HentPersonResponse {
        return pdlWebClient.post()
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenService.getAccessTokenFrontendSent()}"
            )
            .header("Nav-Consumer-Token", "Bearer ${tokenService.getStsSystembrukerToken()}")
            .bodyValue(hentPersonQuery(fnrList))
            .retrieve()
            .bodyToMono<HentPersonResponse>()
            .block() ?: throw RuntimeException("Person not found")
    }
}
