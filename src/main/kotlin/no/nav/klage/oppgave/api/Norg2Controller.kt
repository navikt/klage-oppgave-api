package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.clients.Norg2Client
import no.nav.klage.oppgave.domain.norg2.Enhet
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class Norg2Controller(
    val norg2Client: Norg2Client
) {

    @GetMapping("/enhet/{enhetNr}")
    fun getEnhet(@PathVariable("enhetNr") enhetNr: String): Enhet = norg2Client.fetchEnhet(enhetNr)

}
