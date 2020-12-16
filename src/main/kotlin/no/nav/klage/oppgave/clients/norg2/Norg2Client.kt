package no.nav.klage.oppgave.clients.norg2

import no.nav.klage.oppgave.config.CacheWithRedisConfiguration.Companion.ENHET_CACHE
import org.springframework.cache.annotation.Cacheable
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class Norg2Client(private val norg2WebClient: WebClient) {

    @Retryable
    @Cacheable(ENHET_CACHE)
    fun fetchEnhet(enhetNr: String): Enhet =
        norg2WebClient.get()
            .uri("/enhet/{enhetNr}", enhetNr)
            .retrieve()
            .bodyToMono<EnhetResponse>()
            .block()
            ?.asEnhet() ?: throw RuntimeException("Enhet not found") // TODO: Handle with 404

}
