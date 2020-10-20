package no.nav.klage.oppgave.service.unleash

import no.finn.unleash.UnleashContext
import no.finn.unleash.strategy.Strategy
import no.nav.klage.oppgave.clients.AxsysClient
import org.springframework.stereotype.Component

@Component
class ByEnhetStrategy(val axsys : AxsysClient) : Strategy {

    companion object {
        const val PARAM = "valgtEnhet"
    }

    override fun getName(): String {
        return "byEnhet"
    }

    override fun isEnabled(parameters: Map<String, String>): Boolean {
        return false
    }

    override fun isEnabled(parameters: Map<String, String>, unleashContext: UnleashContext): Boolean =
        unleashContext.userId.map {
            val saksbehandlersEnheter: List<String> = getSaksbehandlersEnheter(unleashContext)
            val enabledEnheter: List<String>? = getEnabledEnheter(parameters)
            enabledEnheter?.intersect(saksbehandlersEnheter)?.isNotEmpty() ?: false
        }.orElse(false)

    private fun getSaksbehandlersEnheter(unleashContext: UnleashContext) =
        axsys.getTilgangerForSaksbehandler(unleashContext.userId.get()).enheter.asList().map { it.enhetId }

    private fun getEnabledEnheter(parameters: Map<String, String>?): List<String>? =
        parameters?.get("PARAM")?.split(',')

}