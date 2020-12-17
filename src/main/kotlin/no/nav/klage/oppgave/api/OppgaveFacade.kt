package no.nav.klage.oppgave.api

import no.finn.unleash.Unleash
import no.nav.klage.oppgave.api.mapper.OppgaveViewMapper
import no.nav.klage.oppgave.api.view.Oppgave
import no.nav.klage.oppgave.api.view.OppgaverRespons
import no.nav.klage.oppgave.domain.EsOppgaveListVisningAdapter
import no.nav.klage.oppgave.domain.GosysOppgaveListVisningAdapter
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.service.ElasticsearchService
import no.nav.klage.oppgave.service.OppgaveService
import org.springframework.stereotype.Service

@Service
class OppgaveFacade(
    val oppgaveService: OppgaveService,
    val elasticsearchService: ElasticsearchService,
    val unleash: Unleash,
    val oppgaveViewMapper: OppgaveViewMapper
) {

    fun searchOppgaver(oppgaverSearchCriteria: OppgaverSearchCriteria): OppgaverRespons {
        if (unleash.isEnabled("klage.sokMedES")) {
            val esResponse = elasticsearchService.oppgaveSearch(oppgaverSearchCriteria)
            return OppgaverRespons(
                antallTreffTotalt = esResponse.totalHits,
                oppgaver = oppgaveViewMapper.mapOppgaverToView(
                    esResponse.searchHits.map { it.content }.map { EsOppgaveListVisningAdapter(it) },
                    oppgaverSearchCriteria.isProjectionUtvidet()
                )
            )
        } else {
            val oppgaveResponse = oppgaveService.searchOppgaver(oppgaverSearchCriteria)
            return OppgaverRespons(
                antallTreffTotalt = oppgaveResponse.antallTreffTotalt.toLong(),
                oppgaver = oppgaveViewMapper.mapOppgaverToView(
                    oppgaveResponse.oppgaver.map { GosysOppgaveListVisningAdapter(it) },
                    oppgaverSearchCriteria.isProjectionUtvidet()
                )
            )
        }
    }

    fun assignOppgave(oppgaveId: Long, saksbehandlerIdent: String?, oppgaveVersjon: Int) {
        oppgaveService.assignOppgave(oppgaveId, saksbehandlerIdent, oppgaveVersjon)
    }

    fun getOppgave(oppgaveId: Long): Oppgave {
        val oppgaveBackend = oppgaveService.getOppgave(oppgaveId)
        return oppgaveViewMapper.mapOppgaveToView(GosysOppgaveListVisningAdapter(oppgaveBackend), true)
    }

}