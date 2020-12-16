package no.nav.klage.oppgave.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.view.Enhet
import no.nav.klage.oppgave.clients.axsys.Tilganger
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
class SaksbehandlerController(private val saksbehandlerService: SaksbehandlerService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent klageenheter for en ansatt",
        notes = "Henter alle klageenheter som saksbehandler er knyttet til."
    )
    @GetMapping("/ansatte/{navIdent}/enheter", produces = ["application/json"])
    fun getEnheter(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String
    ): List<Enhet> {
        logger.debug("getEnheter is requested by $navIdent")
        return saksbehandlerService.getTilgangerForSaksbehandler().toEnheter()
    }

    private fun Tilganger.toEnheter() =
        this.enheter.map {
            Enhet(
                id = it.enhetId,
                navn = it.navn
            )
        }
}

