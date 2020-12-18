package no.nav.klage.oppgave.api.mapper

import no.nav.klage.oppgave.clients.gosys.Oppgave
import no.nav.klage.oppgave.clients.pdl.HentPersonBolkResult
import no.nav.klage.oppgave.clients.pdl.PdlClient
import no.nav.klage.oppgave.domain.OppgaveListVisning
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import no.nav.klage.oppgave.api.view.Oppgave as OppgaveView

@Service
class OppgaveViewMapper(val pdlClient: PdlClient) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapOppgaveToView(oppgaveBackend: Oppgave, fetchPersoner: Boolean): OppgaveView {
        return mapOppgaverToView(listOf(oppgaveBackend), fetchPersoner).single()
    }

    fun mapOppgaverToView(oppgaverBackend: List<OppgaveListVisning>, fetchPersoner: Boolean): List<OppgaveView> {
        val personer = mutableMapOf<String, OppgaveView.Person>()
        if (fetchPersoner) {
            personer.putAll(getPersoner(getFnr(oppgaverBackend)))
        }

        return oppgaverBackend.map { oppgaveBackend ->
            OppgaveView(
                id = oppgaveBackend.id.toString(),
                person = if (fetchPersoner) {
                    personer[oppgaveBackend.fnr] ?: OppgaveView.Person("Mangler fnr", "Mangler navn")
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

    private fun getFnr(oppgaver: List<OppgaveListVisning>) =
        oppgaver.mapNotNull { it.fnr }

    private fun getPersoner(fnrList: List<String>): Map<String, OppgaveView.Person> {
        logger.debug("getPersoner is called with {} fnr", fnrList.size)
        secureLogger.debug("getPersoner with fnr: {}", fnrList)

        val people = pdlClient.getPersonInfo(fnrList).data?.hentPersonBolk ?: emptyList()

        logger.debug("pdl returned {} people", people.size)
        secureLogger.debug("pdl returned {}", people)

        val fnrToPerson: Map<String, no.nav.klage.oppgave.api.view.Oppgave.Person> = people.map {
            val fnr = it.ident
            fnr to OppgaveView.Person(
                fnr = fnr,
                navn = it.person.navn.firstOrNull()?.toName() ?: "mangler navn"
            )
        }.toMap()
        return fnrList.map {
            if (fnrToPerson.containsKey(it)) {
                Pair(it, fnrToPerson.getValue(it))
            } else {
                Pair(it, OppgaveView.Person(fnr = it, navn = "Mangler navn"))
            }
        }.toMap()
    }

    private fun HentPersonBolkResult.Person.Navn.toName() = "$fornavn $etternavn"
}