package no.nav.klage.oppgave.clients

import no.nav.klage.oppgave.domain.norg2.Enhet
import no.nav.klage.oppgave.domain.norg2.EnhetResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class Norg2Client(private val norg2WebClient: WebClient) {

    fun fetchEnhet(enhetNr: String): Enhet =
        norg2WebClient.get()
            .uri("/enhet/{enhetNr}", enhetNr)
            .retrieve()
            .bodyToMono<EnhetResponse>()
            .block()
            ?.asEnhet() ?: throw RuntimeException("Enhet not found") // TODO: Handle with 404

}
