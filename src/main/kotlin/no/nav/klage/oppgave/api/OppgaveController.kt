package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.Tilganger
import no.nav.klage.oppgave.domain.view.OppgaveView
import no.nav.klage.oppgave.service.OppgaveSearchCriteria
import no.nav.klage.oppgave.service.OppgaveService
import no.nav.klage.oppgave.service.unleash.TokenUtils
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
@ProtectedWithClaims(issuer = ISSUER_AAD)
class OppgaveController(val oppgaveService: OppgaveService, val tokenUtils: TokenUtils) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/tilganger")
    fun getTilganger(): Tilganger {
        return oppgaveService.getTilgangerForSaksbehandler()
    }

    @GetMapping("/oppgaver")
    fun getOppgaver(): List<OppgaveView> {
        logger.debug("getOppgaver is requested")
        return oppgaveService.getOppgaver()
    }

    @GetMapping("/oppgaver/ikketildelt")
    fun findIkkeTildelteOppgaver(): List<OppgaveView> {
        logger.debug("findIkkeTildelteOppgaver is requested")
        return oppgaveService.searchOppgaver(OppgaveSearchCriteria(erTildeltSaksbehandler = false))
    }

    @GetMapping("/oppgaver/tildelt/{ident}")
    fun findTildelteOppgaver(@PathVariable(name = "ident", required = false) ident: String?): List<OppgaveView> {
        logger.debug("findTildelteOppgaver is requested")
        return oppgaveService.searchOppgaver(
            OppgaveSearchCriteria(
                erTildeltSaksbehandler = true,
                saksbehandler = ident
            )
        )
    }
}