package no.nav.klage.oppgave.clients

import no.nav.klage.oppgave.domain.MicrosoftGraphIdentResponse
import no.nav.klage.oppgave.domain.MicrosoftGraphNameResponse
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import kotlin.system.measureTimeMillis

@Component
class MicrosoftGraphClient(private val microsoftGraphWebClient: WebClient) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        val saksbehandlerNameCache = mutableMapOf<String, String>()
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
            .bodyToMono<MicrosoftGraphIdentResponse>()
            .block()?.onPremisesSamAccountName ?: throw RuntimeException("NavIdent could not be fetched")
    }

    fun getNamesForSaksbehandlere(identer: Set<String>, accessToken: String): Map<String, String> {
        logger.debug("Fetching names for saksbehandlere from Microsoft Graph. Identer: {}", identer)

        //TODO remove
        logger.debug("accessToken for fetching names: {}", accessToken)

        val identerNotInCache = identer.toMutableSet()
        identerNotInCache -= saksbehandlerNameCache.keys
        logger.debug("Fetching identer not in cache: {}", identerNotInCache)

        val measuredTimeMillis = measureTimeMillis {
            saksbehandlerNameCache += getDisplayNames(identerNotInCache, accessToken)
        }
        logger.debug("It took {} millis to fetch names", measuredTimeMillis)

        return saksbehandlerNameCache
    }

    private fun getDisplayNames(idents: Set<String>, accessToken: String): Map<String, String> {
        return try {
            val response = microsoftGraphWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/users")
                        .queryParam("\$filter", "mailnickname in '${idents.joinToString()}'")
                        .queryParam("\$select", "onPremisesSamAccountName,displayName")
                        .build()
                }.header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono<MicrosoftGraphNameResponse>()
                .block().also { logger.debug("getDisplayName returned {}", it) }

            response?.value?.mapNotNull {
                if (it.onPremisesSamAccountName == null || it.displayName == null) {
                    null
                } else {
                    it.onPremisesSamAccountName to it.displayName
                }
            }?.toMap() ?: emptyMap()
        } catch (e: Exception) {
            logger.warn("Could not fetch displayname for idents $idents", e)
            emptyMap()
        }
    }
}