package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.Enhet
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.domain.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.data.repository.query.Param
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
class SaksbehandlerController(private val saksbehandlerService: SaksbehandlerService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Hent klageenheter for en ansatt",
        description = "Henter alle klageenheter som saksbehandler er knyttet til."
    )
    @GetMapping("/ansatte/{navIdent}/enheter", produces = ["application/json"])
    fun getEnheter(
        @Param(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String
    ): List<Enhet> {
        logger.debug("getEnheter is requested by $navIdent")
        return saksbehandlerService.getTilgangerForSaksbehandler().toEnheter()
    }

    private fun EnheterMedLovligeTemaer.toEnheter() =
        this.enheter.map { enhet ->
            Enhet(
                id = enhet.enhetId,
                navn = enhet.navn,
                lovligeTemaer = enhet.temaer.map { tema -> tema.id }
            )
        }
}

