package no.nav.klage.oppgave.clients

import brave.Tracer
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_KLAGE
import no.nav.klage.oppgave.domain.gosys.OppgaveResponse
import no.nav.klage.oppgave.service.OppgaveSokekriterier
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriBuilder
import java.net.URI

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

    fun oppgaverSok(oppgaveSokekriterier: OppgaveSokekriterier): OppgaveResponse {
        logger.debug("Fetching oppgaver")

        return oppgaveWebClient.get()
            .uri { uriBuilder -> oppgaveSokekriterier.buildUri(uriBuilder) }
            .header("Authorization", "Bearer ${stsClient.oidcToken()}")
            .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
            .header("Nav-Consumer-Id", applicationName)
            .retrieve()
            .bodyToMono<OppgaveResponse>()
            .block() ?: throw RuntimeException("Oppgaver could not be fetched")
    }

    fun getOppgaver(): OppgaveResponse {
        logger.debug("Fetching oppgaver")

        return oppgaveWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .queryParam("statuskategori", STATUSKATEGORI_AAPEN)
                    .queryParam("tema", TEMA_SYK)
                    .queryParam("behandlingstype", BEHANDLINGSTYPE_KLAGE)
                    .queryParam("behandlingstype", BEHANDLINGSTYPE_FEILUTBETALING)
                    .build()
            }
            .header("Authorization", "Bearer ${stsClient.oidcToken()}")
            .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
            .header("Nav-Consumer-Id", applicationName)
            .retrieve()
            .bodyToMono<OppgaveResponse>()
            .block() ?: throw RuntimeException("Oppgaver could not be fetched")
    }


    private fun OppgaveSokekriterier.buildUri(uriBuilder: UriBuilder): URI {
        var uriBuilder = uriBuilder
            .queryParam("statuskategori", OppgaveClient.STATUSKATEGORI_AAPEN)

        this.type?.let { uriBuilder = uriBuilder.queryParam("tema", mapType(it)) }
        this.ytelse?.let { uriBuilder = uriBuilder.queryParam("tema", mapYtelseTilTema(it)) }
        this.erTildeltSaksbehandler?.let { uriBuilder = uriBuilder.queryParam("tildeltRessurs", it) }
        this.saksbehandler?.let { uriBuilder = uriBuilder.queryParam("tilordnetRessurs", it) }

        return uriBuilder.build()
    }

    private fun mapType(type: String): String {
        //TODO
        return BEHANDLINGSTYPE_KLAGE
    }

    private fun mapYtelseTilTema(type: String): String {
        //TODO
        return TEMA_SYK
    }
}






