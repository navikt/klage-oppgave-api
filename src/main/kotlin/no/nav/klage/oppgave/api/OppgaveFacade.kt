package no.nav.klage.oppgave.api

import no.finn.unleash.Unleash
import no.nav.klage.oppgave.api.internal.OppgaveKopiAPIModel
import no.nav.klage.oppgave.api.mapper.OppgaveMapper
import no.nav.klage.oppgave.api.view.AntallUtgaatteFristerResponse
import no.nav.klage.oppgave.api.view.OppgaverRespons
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.service.OppgaveKopiService
import no.nav.klage.oppgave.service.OppgaveService
import no.nav.klage.oppgave.service.OppgaveStatistikkService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class OppgaveFacade(
    private val oppgaveService: OppgaveService,
    private val oppgaveMapper: OppgaveMapper,
    private val oppgaveKopiService: OppgaveKopiService,
    private val elasticsearchRepository: ElasticsearchRepository,
    private val oppgaveStatistikkService: OppgaveStatistikkService,
    private val unleash: Unleash
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun searchOppgaver(oppgaverSearchCriteria: OppgaverSearchCriteria): OppgaverRespons {
        if (unleash.isEnabled("klage.sokMedES")) {
            val esResponse = elasticsearchRepository.findByCriteria(oppgaverSearchCriteria)
            return OppgaverRespons(
                antallTreffTotalt = esResponse.totalHits.toInt(),
                oppgaver = oppgaveMapper.mapEsOppgaverToView(
                    esResponse.searchHits.map { it.content },
                    oppgaverSearchCriteria.isProjectionUtvidet()
                )
            )
        } else {
            val oppgaveResponse = oppgaveService.searchOppgaver(oppgaverSearchCriteria)
            return OppgaverRespons(
                antallTreffTotalt = oppgaveResponse.antallTreffTotalt,
                oppgaver = oppgaveMapper.mapOppgaverToView(
                    oppgaveResponse.oppgaver,
                    oppgaverSearchCriteria.isProjectionUtvidet()
                )
            )
        }
    }

    fun assignOppgave(oppgaveId: Long, saksbehandlerIdent: String?, oppgaveVersjon: Int?) {
        oppgaveService.assignOppgave(oppgaveId, saksbehandlerIdent, oppgaveVersjon)
    }

    fun saveOppgaveKopi(oppgave: OppgaveKopiAPIModel) {
        oppgaveKopiService.saveOppgaveKopi(oppgaveMapper.mapOppgaveKopiAPIModelToOppgaveKopi(oppgave))
        if (unleash.isEnabled("klage.indekserMedES")) {
            try {
                elasticsearchRepository.save(oppgaveMapper.mapOppgaveKopiAPIModelToEsOppgave(oppgave))
            } catch (e: Exception) {
                if (e.message?.contains("version_conflict_engine_exception") == true) {
                    logger.info("Later version already indexed, ignoring this..")
                } else {
                    logger.error("Unable to index OppgaveKopi, see securelogs for details")
                    securelogger.error("Unable to index OppgaveKopi", e)
                }
            }
        }
    }

    fun countUtgaatteFrister(searchCriteria: OppgaverSearchCriteria): AntallUtgaatteFristerResponse =
        AntallUtgaatteFristerResponse(antall = oppgaveStatistikkService.klagerOverFrist(searchCriteria))
}