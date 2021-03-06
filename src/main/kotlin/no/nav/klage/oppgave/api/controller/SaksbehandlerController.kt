package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.view.Enhet
import no.nav.klage.oppgave.api.view.Medunderskriver
import no.nav.klage.oppgave.api.view.Medunderskrivere
import no.nav.klage.oppgave.api.view.ValgtEnhetInput
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.domain.EnhetMedLovligeTemaer
import no.nav.klage.oppgave.domain.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.exceptions.NotMatchingUserException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Api(tags = ["kabal-api"])
class SaksbehandlerController(
    private val saksbehandlerService: SaksbehandlerService,
    private val environment: Environment,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) {

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
        val enheter = saksbehandlerService.getEnheterMedTemaerForSaksbehandler().toEnheter()
        logEnheter(enheter, navIdent)
        return enheter
    }

    @ApiOperation(
        value = "Setter valgt klageenhet for en ansatt",
        notes = "Setter valgt klageenhet som den ansatte jobber med. Må være en i lista over mulige enheter"
    )
    @PutMapping("/ansatte/{navIdent}/valgtenhet", produces = ["application/json"])
    fun setValgtEnhet(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @RequestBody input: ValgtEnhetInput
    ): Enhet {
        validateNavIdent(navIdent)
        return saksbehandlerService.storeValgtEnhetId(navIdent, input.enhetId).toEnhet()
    }

    @ApiOperation(
        value = "Henter valgt klageenhet for en ansatt",
        notes = "Henter valgt klageenhet som den ansatte jobber med. Er fra lista over mulige enheter"
    )
    @GetMapping("/ansatte/{navIdent}/valgtenhet", produces = ["application/json"])
    fun getValgtEnhet(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String
    ): Enhet {
        return saksbehandlerService.findValgtEnhet(navIdent).toEnhet()
    }

    @ApiOperation(
        value = "Hent medunderskriver for en ansatt",
        notes = "Henter alle medunderskrivere som saksbehandler er knyttet til for et gitt tema."
    )
    @GetMapping("/ansatte/{navIdent}/medunderskrivere/{tema}", produces = ["application/json"])
    fun getMedunderskrivere(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Tema man trenger medunderskrivere for")
        @PathVariable tema: String
    ): Medunderskrivere {
        logger.debug("getMedunderskrivere is requested by $navIdent")
        return if (environment.activeProfiles.contains("prod-gcp")) {
            saksbehandlerService.getMedunderskrivere(navIdent, Tema.of(tema))
        } else Medunderskrivere(
            tema,
            listOf(
                Medunderskriver("Z994488", "F_Z994488, E_Z994488"),
                Medunderskriver("Z994330", "F_Z994330 E_Z994330"),
                Medunderskriver("Z994861", "F_Z994861 E_Z994861"),
                Medunderskriver("Z994864", "F_Z994864 E_Z994864"),
                Medunderskriver("Z994863", "F_Z994863 E_Z994863"),
                Medunderskriver("Z994862", "F_Z994862 E_Z994862"),
            ).filter { it.ident != navIdent }
        )
    }

    private fun logEnheter(enheter: List<Enhet>, navIdent: String) {
        enheter.forEach { enhet ->
            logger.debug(
                "{} has access to {} ({}) with temaer {}",
                navIdent,
                enhet.id,
                enhet.navn,
                enhet.lovligeTemaer.joinToString(separator = ",")
            )
        }
    }

    private fun EnheterMedLovligeTemaer.toEnheter() = this.enheter.map { enhet -> enhet.toEnhet() }

    private fun EnhetMedLovligeTemaer.toEnhet() =
        Enhet(
            id = enhetId,
            navn = navn,
            lovligeTemaer = temaer.map { tema -> tema.id }
        )

    private fun validateNavIdent(navIdent: String) {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        if (innloggetIdent != navIdent) {
            throw NotMatchingUserException(
                "logged in user does not match sent in user. " +
                        "Logged in: $innloggetIdent, sent in: $navIdent"
            )
        }
    }

}

