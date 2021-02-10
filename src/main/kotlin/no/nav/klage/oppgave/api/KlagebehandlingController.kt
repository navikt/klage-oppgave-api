package no.nav.klage.oppgave.api

import io.swagger.annotations.Api
import no.nav.klage.oppgave.api.view.GrunnInput
import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.api.view.KvalitetsvurderingView
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.kodeverk.Grunn
import no.nav.klage.oppgave.exceptions.ValidationException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingController(
    private val klagebehandlingFacade: KlagebehandlingFacade,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/klagebehandlinger/{id}")
    fun getKlagebehandling(
        @PathVariable("id") oppgaveId: Long
    ): KlagebehandlingView {
        logger.debug(
            "getKlagebehandling is requested by ident {} for klagebehandlingid {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            oppgaveId
        )
        return klagebehandlingFacade.getKlagebehandling(oppgaveId)
    }

    @GetMapping("/oppgaver/{id}/klagebehandling/klage")
    fun getKlagebehandlingByOppgave(
        @PathVariable("id") oppgaveId: Long
    ): KlagebehandlingView {
        logger.debug(
            "getKlagebehandling is requested by ident {} for oppgaveId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            oppgaveId
        )
        return klagebehandlingFacade.getKlagebehandling(oppgaveId)
    }

    @GetMapping("/oppgaver/{id}/klagebehandling/kvalitetsvurdering")
    fun getKvalitetsvurdering(
        @PathVariable("id") oppgaveId: Long
    ): KvalitetsvurderingView {
        logger.debug(
            "getKvalitetsvurdering is requested by ident {} for oppgaveId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            oppgaveId
        )
        return klagebehandlingFacade.getKvalitetsvurdering(oppgaveId)
    }

    @PutMapping("/oppgaver/{id}/klagebehandling/kvalitetsvurdering/grunn")
    fun setGrunn(
        @PathVariable("id") oppgaveId: Long,
        @RequestBody input: GrunnInput
    ): KvalitetsvurderingView {
        logger.debug(
            "getKvalitetsvurdering is requested by ident {} for oppgaveId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            oppgaveId
        )
        val grunn = parseAndValidateGrunn(input)
        return klagebehandlingFacade.setKvalitetsvurderingGrunn(oppgaveId, grunn)
    }

    private fun parseAndValidateGrunn(input: GrunnInput) = try {
        Grunn.of(input.grunn)
    } catch (e: Exception) {
        logger.error("${input.grunn} is not a valid Grunn")
        throw ValidationException("${input.grunn} is not a valid Grunn")
    }

}