package no.nav.klage.oppgave.clients

import brave.Tracer
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_KLAGE
import no.nav.klage.oppgave.domain.gosys.Oppgave
import no.nav.klage.oppgave.domain.gosys.OppgaveResponse
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class OppgaveClient(
    private val oppgaveWebClient: WebClient,
    private val stsClient: StsClient,
    private val tracer: Tracer,
    @Value("\${spring.application.name}") val applicationName: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        const val TEMA_SYK = "SYK"
        const val STATUSKATEGORI_AAPEN = "AAPEN"
    }

    fun getOppgaver(): OppgaveResponse {
        logger.debug("Fetching oppgaver")
        
        val allOppgaver = mutableListOf<Oppgave>()
        var numberOfOppgaverRetrieved : Int = 0

        do {
            val onePage = getOnePage(numberOfOppgaverRetrieved)
            allOppgaver.addAll(onePage.oppgaver)
            numberOfOppgaverRetrieved+= onePage.oppgaver.size
        } while( numberOfOppgaverRetrieved < onePage.antallTreffTotalt)

        return OppgaveResponse(numberOfOppgaverRetrieved, allOppgaver)
    }

    private fun getOnePage(offset: Int): OppgaveResponse {
        return oppgaveWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .queryParam("statuskategori", STATUSKATEGORI_AAPEN)
                    .queryParam("tema", TEMA_SYK)
                    .queryParam("behandlingstype", BEHANDLINGSTYPE_KLAGE)
                    .queryParam("behandlingstype", BEHANDLINGSTYPE_FEILUTBETALING)
                    .queryParam("limit", 100)
                    .queryParam("offset", offset)
                    .build()
            }
            .header("Authorization", "Bearer ${stsClient.oidcToken()}")
            .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
            .header("Nav-Consumer-Id", applicationName)
            .retrieve()
            .bodyToMono<OppgaveResponse>()
            .block() ?: throw RuntimeException("Oppgaver could not be fetched")
    }
}



