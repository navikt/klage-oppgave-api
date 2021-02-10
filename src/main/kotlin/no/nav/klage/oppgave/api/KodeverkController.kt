package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.api.view.KodeverkResponse
import no.nav.klage.oppgave.util.getLogger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class KodeverkController {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/kodeverk", produces = ["application/json"])
    fun getKodeverk(): KodeverkResponse {
        return KodeverkResponse()
    }
}