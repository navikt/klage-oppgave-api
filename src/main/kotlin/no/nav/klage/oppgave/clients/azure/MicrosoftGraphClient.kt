package no.nav.klage.oppgave.clients.azure

import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import org.springframework.cache.annotation.Cacheable
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class MicrosoftGraphClient(
    private val microsoftGraphWebClient: WebClient,
    private val tokenUtil: TokenUtil
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Retryable
    fun getNavIdentForAuthenticatedUser(): String {
        logger.debug("Fetching navIdent from Microsoft Graph")

        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/me")
                    .queryParam("\$select", "onPremisesSamAccountName")
                    .build()
            }.header("Authorization", "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithGraphScope()}")

            .retrieve()
            .bodyToMono<MicrosoftGraphIdentResponse>()
            .block()?.onPremisesSamAccountName ?: throw RuntimeException("NavIdent could not be fetched")
    }

    @Retryable
    fun getAllDisplayNames(idents: List<List<String>>): Map<String, String> {
        val queryString = idents.map {
            it.joinToString(separator = "','", prefix = "('", postfix = "')")
        }

        val data = Flux.fromIterable(queryString)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap {
                getDisplayNames(it)
            }
            .ordered { _: MicrosoftGraphNameResponse, _: MicrosoftGraphNameResponse -> 1 }.toIterable()

        return data.flatMap {
            it.value ?: emptyList()
        }.mapNotNull {
            if (it.onPremisesSamAccountName == null || it.displayName == null) {
                null
            } else {
                it.onPremisesSamAccountName to it.displayName
            }
        }.toMap()
    }

    private fun getDisplayNames(idents: String): Mono<MicrosoftGraphNameResponse> {
        return try {
            microsoftGraphWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/users")
                        .queryParam("\$filter", "mailnickname in $idents")
                        .queryParam("\$select", "onPremisesSamAccountName,displayName")
                        .build()
                }.header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithGraphScope()}")
                .retrieve()
                .bodyToMono()
        } catch (e: Exception) {
            logger.warn("Could not fetch displayname for idents: $idents", e)
            Mono.empty()
        }
    }

    @Retryable
    @Cacheable(CacheWithJCacheConfiguration.GROUPMEMBERS_CACHE)
    fun getGroupMembers(groupid: String): List<String> {
        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/groups/{groupid}/members")
                    .queryParam("\$select", "mailnickname,onPremisesSamAccountName,displayName")
                    .build(groupid)
            }
            .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithGraphScope()}")
            .retrieve()
            .bodyToMono<MicrosoftGraphGroupMembersResponse>().block().value!!
            .map { logger.debug("Har funnet $it"); it }
            .map { it.onPremisesSamAccountName }
            .filterNotNull()
    }

    @Retryable
    fun getRoller(ident: String): List<String> {
        return try {
            val idents = listOf(ident).joinToString(separator = "','", prefix = "('", postfix = "')")
            val user =
                microsoftGraphWebClient.get()
                    .uri { uriBuilder ->
                        uriBuilder
                            .path("/users")
                            .queryParam("\$filter", "mailnickname in $idents")
                            .queryParam("\$select", "userPrincipalName")
                            .build()
                    }
                    .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithGraphScope()}")
                    .retrieve()
                    .bodyToMono<MicrosoftGraphUsersResponse>().block().value!!.first()

            val userPrincipalName = user.userPrincipalName
            val aadGroups: List<Group> = microsoftGraphWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/users/{userPrincipalName}/memberOf")
                        .build(userPrincipalName)
                }
                .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithGraphScope()}")
                .retrieve()
                .bodyToMono<MicrosoftGraphMemberOfResponse>().block().value
            aadGroups.map { it.id }
        } catch (e: Exception) {
            logger.error("Failed to retrieve AAD groups for $ident", e)
            emptyList()
        }
    }
}