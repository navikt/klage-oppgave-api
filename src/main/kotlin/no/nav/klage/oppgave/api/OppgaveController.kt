package no.nav.klage.oppgave.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.mapper.OppgaverQueryParamsMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.exceptions.NotMatchingUserException
import no.nav.klage.oppgave.exceptions.OppgaveIdWrongFormatException
import no.nav.klage.oppgave.exceptions.OppgaveVersjonWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class OppgaveController(
    private val oppgaveFacade: OppgaveFacade,
    private val oppgaverQueryParamsMapper: OppgaverQueryParamsMapper,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent oppgaver for en ansatt",
        notes = "Henter alle oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/oppgaver", produces = ["application/json"])
    fun getOppgaver(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: OppgaverQueryParams
    ): OppgaverRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)
        return oppgaveFacade.searchOppgaver(
            oppgaverQueryParamsMapper.toSearchCriteria(navIdent, queryParams)
        )
    }

    @PostMapping("/ansatte/{navIdent}/oppgaver/{id}/saksbehandlertildeling")
    fun assignSaksbehandler(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Id til en oppgave")
        @PathVariable("id") oppgaveId: String,
        @RequestBody saksbehandlertildeling: Saksbehandlertildeling
    ): ResponseEntity<Void> {
        logger.debug("assignSaksbehandler is requested for oppgave: {}", oppgaveId)
        oppgaveFacade.assignOppgave(
            oppgaveId.toLongOrException(),
            saksbehandlertildeling.navIdent,
            saksbehandlertildeling.oppgaveversjon.toIntOrException()
        )

        val uri = MvcUriComponentsBuilder
            .fromMethodName(OppgaveController::class.java, "getOppgave", navIdent, oppgaveId)
            .buildAndExpand(oppgaveId).toUri()
        return ResponseEntity.noContent().location(uri).build()
    }

    @PostMapping("/ansatte/{navIdent}/oppgaver/{id}/saksbehandlerfradeling")
    fun unassignSaksbehandler(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Id til en oppgave")
        @PathVariable("id") oppgaveId: String,
        @RequestBody saksbehandlerfradeling: Saksbehandlerfradeling
    ): ResponseEntity<Void> {
        logger.debug("unassignSaksbehandler is requested for oppgave: {}", oppgaveId)
        oppgaveFacade.assignOppgave(
            oppgaveId.toLongOrException(),
            null,
            saksbehandlerfradeling.oppgaveversjon.toIntOrException()
        )

        val uri = MvcUriComponentsBuilder
            .fromMethodName(OppgaveController::class.java, "getOppgave", navIdent, oppgaveId)
            .buildAndExpand(oppgaveId).toUri()
        return ResponseEntity.noContent().location(uri).build()
    }

    @GetMapping("/ansatte/{navIdent}/oppgaver/{id}")
    fun getOppgave(
        @PathVariable navIdent: String,
        @PathVariable("id") oppgaveId: String
    ): Oppgave {
        logger.debug("getOppgave is requested: {}", oppgaveId)
        return oppgaveFacade.getOppgave(oppgaveId.toLongOrException())
    }

    private fun String?.toLongOrException() =
        this?.toLongOrNull() ?: throw OppgaveIdWrongFormatException("OppgaveId could not be parsed as a Long")

    private fun String?.toIntOrException() =
        this?.toIntOrNull() ?: throw OppgaveVersjonWrongFormatException("Oppgaveversjon could not be parsed as an Int")

    private fun validateNavIdent(navIdent: String) {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        if (innloggetIdent != navIdent) {
            throw NotMatchingUserException(
                "logged in user does not match sent in user. " +
                        "Logged in: $innloggetIdent, sent in: $navIdent"
            )
        }
    }

    //    @PutMapping("/oppgaver/{id}/hjemmel")
//    fun setHjemmel(
//        @PathVariable("id") oppgaveId: String,
//        @RequestBody hjemmelUpdate: HjemmelUpdate
//    ): ResponseEntity<OppgaveView> {
//        logger.debug("setHjemmel is requested")
//        val oppgave =
//            oppgaveService.setHjemmel(oppgaveId.toLongOrException(), hjemmelUpdate.hjemmel, hjemmelUpdate.versjon)
//        val uri = MvcUriComponentsBuilder
//            .fromMethodName(OppgaveController::class.java, "getOppgave", oppgaveId)
//            .buildAndExpand(oppgaveId).toUri()
//        return ResponseEntity.ok().location(uri).body(oppgave)
//    }
//

}