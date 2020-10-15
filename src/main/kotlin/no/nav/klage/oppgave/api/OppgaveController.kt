package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.Tilganger
import no.nav.klage.oppgave.domain.view.OppgaveView
import no.nav.klage.oppgave.service.OppgaveService
import no.nav.klage.oppgave.service.OppgaveSokekriterier
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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
    fun getOppgaver(): List<OppgaveView> {
        logger.debug("getOppgaver is requested")
        return oppgaveService.getOppgaver()
    }

    @GetMapping("/oppgaver/sok")
    fun oppgaveSok(
            @RequestParam(name = "type", required = false) type: String?,
            @RequestParam(name = "ytelse", required = false) ytelse: String?,
            @RequestParam(name = "hjemmel", required = false) hjemmel: String?,
            @RequestParam(name = "erTildeltSaksbehandler", required = false) erTildeltSaksbehandler: Boolean?,
            @RequestParam(name = "saksbehandler", required = false) saksbehandler: String?,
    ): List<OppgaveView> {
        logger.debug("oppgaveSok is requested")
        return oppgaveService.oppgaveSok(OppgaveSokekriterier(type, ytelse, hjemmel, erTildeltSaksbehandler, saksbehandler))
    }

}