package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.OversendtKlage
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.MottakService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.data.repository.query.Param
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@Tag(name = "kabal-api-external")
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RequestMapping("api")
class ExternalApiController(
    private val mottakService: MottakService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Send inn klage til klageinstans",
        description = "Endepunkt for å registrere en klage/anke som skal behandles av klageinstans"
    )
    @PostMapping("/oversendelse/v1/klage")
    fun sendInnKlageV1(
        @Param(value = "Oversendt klage")
        @Valid @RequestBody oversendtKlage: OversendtKlage
    ) {
        mottakService.createMottakForKlage(oversendtKlage)
    }
}
