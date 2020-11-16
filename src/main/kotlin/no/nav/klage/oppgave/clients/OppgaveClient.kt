package no.nav.klage.oppgave.clients

import brave.Tracer
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.gosys.*
import no.nav.klage.oppgave.domain.view.TYPE_ANKE
import no.nav.klage.oppgave.domain.view.TYPE_KLAGE
import no.nav.klage.oppgave.domain.view.YTELSE_FOR
import no.nav.klage.oppgave.domain.view.YTELSE_SYK
import no.nav.klage.oppgave.exceptions.OppgaveNotFoundException
import no.nav.klage.oppgave.service.HjemmelParsingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriBuilder
import java.lang.System.currentTimeMillis
import java.net.URI

@Component
class OppgaveClient(
    private val oppgaveWebClient: WebClient,
    private val stsClient: StsClient,
    private val tracer: Tracer,
    private val hjemmelParsingService: HjemmelParsingService,
    @Value("\${spring.application.name}") val applicationName: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()

        const val STATUSKATEGORI_AAPEN = "AAPEN"
        const val HJEMMEL = "HJEMMEL"
    }

    @Retryable
    fun getOneSearchPage(oppgaveSearchCriteria: OppgaverSearchCriteria): OppgaveResponse {
        val response = logTimingAndWebClientResponseException("getOneSearchPage") {
            oppgaveWebClient.get()
                .uri { uriBuilder -> oppgaveSearchCriteria.buildUri(uriBuilder) }
                .header("Authorization", "Bearer ${stsClient.oidcToken()}")
                .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
                .header("Nav-Consumer-Id", applicationName)
                .retrieve()
                .bodyToMono<OppgaveResponse>()
                .block() ?: throw RuntimeException("Oppgaver could not be fetched")
        }

        val newOppgaveWithHjemler: List<Oppgave> = response.oppgaver.filter {
            it.metadata?.get(HJEMMEL) == null
        }.mapNotNull {
            val hjemmler = it.beskrivelse?.let {besk -> hjemmelParsingService.extractHjemmel(besk)} ?: listOf()
            if (hjemmler.isNotEmpty()) {
                it.copy(metadata = HashMap(it.metadata).apply { put(HJEMMEL, hjemmler[0]) })
            } else {
                null
            }
        }

        updateOppgave(newOppgaveWithHjemler)

        return response.copy(oppgaver = ArrayList(
            response.oppgaver.filter { oppg ->
                newOppgaveWithHjemler.find {
                    it.id == oppg.id
                } == null
            }).apply {
            addAll(newOppgaveWithHjemler)
        })
    }

    private fun updateOppgave(oppgaverWithHjemler: List<Oppgave>) {
        oppgaverWithHjemler.forEach {
            putOppgave(it.id, EndreOppgave(
                id = it.id,
                tema = it.tema,
                metadata = it.metadata?.toMutableMap(),
                fristFerdigstillelse = it.fristFerdigstillelse,
                versjon = it.versjon
            ))
        }
    }

    private fun OppgaverSearchCriteria.buildUri(origUriBuilder: UriBuilder): URI {
        logger.debug("Search criteria: {}", this)
        val uriBuilder = origUriBuilder
            .queryParam("tildeltEnhetsnr", enhetsnr)
            .queryParam("statuskategori", STATUSKATEGORI_AAPEN)
            .queryParam("offset", offset)
            .queryParam("limit", limit)

        if (typer.isNotEmpty()) {
            typer.forEach {
                uriBuilder.queryParam("behandlingstype", mapType(it))
            }
        } else {
            uriBuilder.queryParam("behandlingstype", mapType(TYPE_KLAGE))
            uriBuilder.queryParam("behandlingstype", mapType(TYPE_ANKE))
        }

        ytelser.forEach {
            uriBuilder.queryParam("tema", mapYtelse(it))
        }

//      Do we need this? ->  uriBuilder.queryParam("tildeltRessurs", true|false)
        saksbehandler?.let {
            uriBuilder.queryParam("tilordnetRessurs", saksbehandler)
        }

        //FRIST is default in oppgave-api.
//        uriBuilder.queryParam("sorteringsfelt", orderBy ?: "frist")
        uriBuilder.queryParam("sorteringsrekkefolge", order ?: OppgaverSearchCriteria.Order.ASC)

        if (hjemler.isNotEmpty()) {
            uriBuilder.queryParam("metadatanokkel", HJEMMEL)
            hjemler.forEach {
                uriBuilder.queryParam("metadataverdi", it)
            }
        }

        val uri = uriBuilder.build()
        logger.info("Making search request with query {}", uri.query)
        return uri
    }

    private fun mapType(type: String): String {
        return when (type) {
            TYPE_KLAGE -> BEHANDLINGSTYPE_KLAGE
            TYPE_ANKE -> BEHANDLINGSTYPE_ANKE
            else -> {
                logger.warn("Unknown type: {}", type)
                type
            }
        }
    }

    private fun mapYtelse(ytelse: String): String {
        return when (ytelse) {
            YTELSE_SYK -> TEMA_SYK
            YTELSE_FOR -> TEMA_FOR
            else -> {
                logger.warn("Unknown ytelse: {}", ytelse)
                ytelse
            }
        }
    }

    @Retryable
    fun putOppgave(
        oppgaveId: Long,
        oppgave: EndreOppgave
    ): Oppgave {
        return logTimingAndWebClientResponseException("putOppgave") {
            oppgaveWebClient.put()
                .uri { uriBuilder ->
                    uriBuilder.pathSegment("{id}").build(oppgaveId)
                }
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer ${stsClient.oidcToken()}")
                .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
                .header("Nav-Consumer-Id", applicationName)
                .bodyValue(oppgave)
                .retrieve()
                .bodyToMono<Oppgave>()
                .block() ?: throw OppgaveNotFoundException("Oppgave could not be put")
        }
    }

    @Retryable
    fun getOppgave(oppgaveId: Long): Oppgave {
        return logTimingAndWebClientResponseException("getOppgave") {
            oppgaveWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder.pathSegment("{id}").build(oppgaveId)
                }
                .header("Authorization", "Bearer ${stsClient.oidcToken()}")
                .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
                .header("Nav-Consumer-Id", applicationName)
                .retrieve()
                .bodyToMono<Oppgave>()
                .block() ?: throw OppgaveNotFoundException("Oppgave could not be fetched")
        }
    }

    private fun <T> logTimingAndWebClientResponseException(methodName: String, function: () -> T): T {
        val start: Long = currentTimeMillis()
        try {
            return function.invoke()
        } catch (ex: WebClientResponseException) {
            logger.warn("Caught WebClientResponseException, see securelogs for details")
            securelogger.error(
                "Got a {} error calling Oppgave {} {} with message {}",
                ex.statusCode,
                ex.request?.method ?: "-",
                ex.request?.uri ?: "-",
                ex.responseBodyAsString
            )
            throw ex
        } catch (rtex: RuntimeException) {
            logger.warn("Caught RuntimeException", rtex)
            throw rtex
        } finally {
            val end: Long = currentTimeMillis()
            logger.info("Method {} took {} millis", methodName, (end - start))
        }
    }
}
