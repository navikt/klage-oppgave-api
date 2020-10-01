package no.nav.klage.oppgave.clients

import no.nav.klage.oppgave.domain.MicrosoftGraphResponse
import no.nav.klage.oppgave.domain.Tilganger
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class MicrosoftGraphClient(private val microsoftGraphWebClient: WebClient) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getNavIdent(accessToken: String): String {
        logger.debug("Fetching navIdent from Microsoft Graph")

        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/me")
                    .queryParam("\$select", "onPremisesSamAccountName")
                    .build()
            }.header("Authorization", "Bearer $accessToken")

            .retrieve()
            .bodyToMono<MicrosoftGraphResponse>()
            .block()?.onPremisesSamAccountName ?: throw RuntimeException("Tilganger could not be fetched")
    }
}