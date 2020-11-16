package no.nav.klage.oppgave.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.OppgaverQueryParams
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.Saksbehandlertildeling
import no.nav.klage.oppgave.domain.view.Oppgave
import no.nav.klage.oppgave.domain.view.OppgaverRespons
import no.nav.klage.oppgave.exceptions.OppgaveIdWrongFormatException
import no.nav.klage.oppgave.exceptions.OppgaveVersjonWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.OppgaveService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class OppgaveController(
    private val oppgaveService: OppgaveService,
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
        debugRollerOgTilganger()
        return oppgaveService.searchOppgaver(navIdent, queryParams.toSearchCriteria())
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
        oppgaveService.assignOppgave(
            oppgaveId.toLongOrException(),
            saksbehandlertildeling.navIdent,
            saksbehandlertildeling.oppgaveversjon.toIntOrException()
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
        TODO()
//        logger.debug("getOppgave is requested: {}", oppgaveId)
//        return oppgaveService.getOppgave(oppgaveId.toLongOrException())
    }

    private fun String?.toLongOrException() =
        this?.toLongOrNull() ?: throw OppgaveIdWrongFormatException("OppgaveId could not be parsed as a Long")

    private fun String?.toIntOrException() =
        this?.toIntOrNull() ?: throw OppgaveVersjonWrongFormatException("Oppgaveversjon could not be parsed as an Int")

    private fun OppgaverQueryParams.toSearchCriteria() = OppgaverSearchCriteria(
        typer = typer,
        ytelser = ytelser,
        hjemler = hjemler,
        order = if (rekkefoelge == OppgaverQueryParams.Rekkefoelge.SYNKENDE) {
            OppgaverSearchCriteria.Order.DESC
        } else {
            OppgaverSearchCriteria.Order.ASC
        },
        offset = start,
        limit = antall,
        saksbehandler = tildeltSaksbehandler,
        projection = if (projeksjon?.name != null) OppgaverSearchCriteria.Projection.valueOf(projeksjon.name) else null
    )

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

    private fun debugRollerOgTilganger() {
        try {
            val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
            innloggetSaksbehandlerRepository.getRoller().forEach {
                logger.debug("Funnet rolle {} for saksbehandler {}", it, innloggetIdent)
            }
            logger.debug("Er {} saksbehandler? {}", innloggetIdent, innloggetSaksbehandlerRepository.erSaksbehandler())
            innloggetSaksbehandlerRepository.getTilgangerForSaksbehandler().enheter.forEach {
                logger.debug("Saksbehandler {} har tilganger {} i enhet {}", innloggetIdent, it.fagomrader, it.enhetId)
            }
        } catch (e: Exception) {
            logger.debug("Klarte ikke å hente ut roller og tilganger", e)
        }
    }

}