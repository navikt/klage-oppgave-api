package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.view.OversendtKlage
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.MottakService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@Api(tags = ["kabal-api-external"])
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RequestMapping("api")
class ExternalApiController(
    private val mottakService: MottakService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Send inn klage til klageinstans",
        notes = "Endepunkt for å registrere en klage/anke som skal behandles av klageinstans"
    )
    @PostMapping("/oversendelse/v1/klage")
    fun sendInnKlageV1(
        @ApiParam(value = "Oversendt klage")
        @Valid @RequestBody oversendtKlage: OversendtKlage
    ) {
        mottakService.createMottakForKlage(oversendtKlage)
    }

    @ApiOperation(
        value = "Hent informasjon om en klagebehandling",
        notes = "Endepunkt for å se detaljert informasjon om en klagebehandling fra "
    )
    @GetMapping("innsyn/v1/behandling/{id}")
    fun InnsynBehandling(
        @ApiParam(value = "Id for klagebehandling")
        @PathVariable("id") nehandlingId: String
    ): String {
        return "Not implemented yet"
    }
}
