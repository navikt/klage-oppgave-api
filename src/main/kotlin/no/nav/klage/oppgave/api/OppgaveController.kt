package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.Tilganger
import no.nav.klage.oppgave.domain.view.OppgaveView
import no.nav.klage.oppgave.service.OppgaveSearchCriteria
import no.nav.klage.oppgave.service.OppgaveService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder

@RestController
@ProtectedWithClaims(issuer = ISSUER_AAD)
class OppgaveController(val oppgaveService: OppgaveService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/tilganger")
    fun getTilganger(): Tilganger {
        return oppgaveService.getTilgangerForSaksbehandler()
    }

    @GetMapping("/oppgaver")
    fun getOppgaver(
        @RequestParam(name = "erTildelt", required = false) erTildelt: Boolean?,
        @RequestParam(name = "saksbehandler", required = false) saksbehandler: String?,
    ): List<OppgaveView> {
        logger.debug("getOppgaver is requested")
        return if (erTildelt == null && saksbehandler == null) {
            oppgaveService.getOppgaver()
        } else {
            oppgaveService.searchOppgaver(
                OppgaveSearchCriteria(
                    erTildeltSaksbehandler = erTildelt,
                    saksbehandler = saksbehandler
                )
            )
        }
    }

    @GetMapping("/oppgaver/{id}")
    fun getOppgave(@PathVariable("id") oppgaveId: Int): OppgaveView {
        logger.debug("getOppgave is requested")
        return oppgaveService.getOppgave(oppgaveId)
    }

    @PutMapping("/oppgaver/{id}/hjemmel")
    fun setHjemmel(@PathVariable("id") oppgaveId: Int, @RequestBody hjemmel: String): ResponseEntity<OppgaveView> {
        logger.debug("setHjemmel is requested")
        val oppgave = oppgaveService.setHjemmel(oppgaveId, hjemmel)
        val uri = MvcUriComponentsBuilder
            .fromMethodName(OppgaveController::class.java, "getOppgave", oppgaveId)
            .buildAndExpand(oppgaveId).toUri()
        return ResponseEntity.ok().location(uri).body(oppgave)
    }

    @GetMapping("/oppgaver/randomhjemler")
    fun endreHjemlerAtRandomViolatingGetRpcStyleAndTotallyNotRestish(): List<OppgaveView> {
        logger.debug("endreHjemlerAtRandomViolatingGetRpcStyleAndTotallyNotRestish is requested")
        return oppgaveService.assignRandomHjemler()
    }
}