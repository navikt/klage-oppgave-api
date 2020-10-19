package no.nav.klage.oppgave.api

import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.nav.klage.oppgave.service.unleash.TokenUtils
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class FeatureToggleController(private val unleash: Unleash, private val tokenUtils: TokenUtils) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/featuretoggle/{toggleName}")
    fun getToggle(@PathVariable("toggleName") toggleName: String): Boolean =
        isEnabled(toggleName)

    @Unprotected
    @GetMapping("/aapenfeaturetoggle/{toggleName}")
    fun getUnprotectedToggle(@PathVariable("toggleName") toggleName: String): Boolean =
        unleash.isEnabled(toggleName, UnleashContext.builder().userId("UINNLOGGET").build())

    fun isEnabled(feature: String): Boolean =
        unleash.isEnabled(feature, contextMedInnloggetBruker())

    private fun contextMedInnloggetBruker(): UnleashContext? =
        UnleashContext.builder().userId(getIdent()).build()

    private fun getIdent() = try {
        tokenUtils.getInnloggetIdent()
    } catch (e: Exception) {
        logger.info("Not able to retrieve token", e)
        "UINNLOGGET"
    }


}
