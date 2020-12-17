package no.nav.klage.oppgave.api.mapper

import no.nav.klage.oppgave.clients.pdl.Navn
import no.nav.klage.oppgave.clients.pdl.PdlClient
import no.nav.klage.oppgave.domain.OppgaveListVisning
import org.springframework.stereotype.Service
import no.nav.klage.oppgave.api.view.Oppgave as OppgaveView

@Service
class OppgaveViewMapper(val pdlClient: PdlClient) {

    fun mapOppgaveToView(oppgaveBackend: OppgaveListVisning, fetchPersoner: Boolean): OppgaveView {
        return mapOppgaverToView(listOf(oppgaveBackend), fetchPersoner).single()
    }

    fun mapOppgaverToView(oppgaverBackend: List<OppgaveListVisning>, fetchPersoner: Boolean): List<OppgaveView> {
        val personer = mutableMapOf<String, OppgaveView.Person>()
        if (fetchPersoner) {
            personer.putAll(getPersoner(oppgaverBackend.mapNotNull { it.fnr }))
        }

        return oppgaverBackend.map { oppgaveBackend ->
            OppgaveView(
                id = oppgaveBackend.id.toString(),
                person = if (fetchPersoner) {
                    oppgaveBackend.fnr?.let { personer[it] } ?: OppgaveView.Person("Mangler fnr", "Mangler navn")
                } else {
                    null
                },
                type = oppgaveBackend.type,
                ytelse = oppgaveBackend.ytelse,
                hjemmel = oppgaveBackend.viktigsteHjemmel,
                frist = oppgaveBackend.fristFerdigstillelse,
                versjon = oppgaveBackend.versjon
            )
        }
    }

    private fun getPersoner(fnrList: List<String>): Map<String, OppgaveView.Person> {
        val people = pdlClient.getPersonInfo(fnrList).data?.hentPersonBolk
        val fnrToPerson: Map<String, no.nav.klage.oppgave.api.view.Oppgave.Person> = people?.map {
            val fnr = it.folkeregisteridentifikator.first().identifikasjonsnummer
            fnr to OppgaveView.Person(
                fnr = fnr,
                navn = it.navn.firstOrNull()?.toName() ?: "Mangler navn"
            )
        }?.toMap() ?: emptyMap()
        return fnrList.map {
            if (fnrToPerson.containsKey(it)) {
                Pair(it, fnrToPerson.getValue(it))
            } else {
                Pair(it, OppgaveView.Person(fnr = it, navn = "Mangler navn"))
            }
        }.toMap()
    }


    private fun Navn.toName() = "$fornavn $etternavn"
}