package no.nav.klage.oppgave.api

import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.nav.klage.oppgave.service.unleash.TokenUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class FeatureToggleController(private val unleash: Unleash, private val tokenUtils: TokenUtils) {

    @GetMapping("/featuretoggle/{toggleName}")
    fun getToggle(@PathVariable("toggleName") toggleName: String): Boolean =
        isEnabled(toggleName)

    fun isEnabled(feature: String): Boolean =
        unleash.isEnabled(feature, contextMedInnloggetBruker())

    private fun contextMedInnloggetBruker(): UnleashContext? =
        UnleashContext.builder().userId(tokenUtils.getInnloggetIdent()).build()
    
}
