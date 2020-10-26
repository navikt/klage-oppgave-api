package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.domain.gosys.EndreOppgave
import no.nav.klage.oppgave.domain.gosys.Oppgave
import no.nav.klage.oppgave.domain.gosys.OppgaveResponse
import no.nav.klage.oppgave.service.OppgaveSearchCriteria
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service

@Service
class OppgaveRepository(private val oppgaveClient: OppgaveClient) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getOppgaver(): OppgaveResponse {
        logger.debug("Fetching oppgaver")

        val allOppgaver = mutableListOf<Oppgave>()
        var numberOfOppgaverRetrieved: Int = 0

        do {
            val onePage = oppgaveClient.getOnePage(numberOfOppgaverRetrieved)
            allOppgaver += onePage.oppgaver
            numberOfOppgaverRetrieved += onePage.oppgaver.size
            logger.debug("Retrieved {} of {} oppgaver", numberOfOppgaverRetrieved, onePage.antallTreffTotalt)
        } while (numberOfOppgaverRetrieved < onePage.antallTreffTotalt)

        return OppgaveResponse(numberOfOppgaverRetrieved, allOppgaver)
    }

    fun searchOppgaver(oppgaveSearchCriteria: OppgaveSearchCriteria): OppgaveResponse {
        logger.debug("Searching for oppgaver")

        val allOppgaver = mutableListOf<Oppgave>()
        var numberOfOppgaverRetrieved: Int = 0

        do {
            val onePage = oppgaveClient.getOneSearchPage(oppgaveSearchCriteria, numberOfOppgaverRetrieved)
            allOppgaver += onePage.oppgaver
            numberOfOppgaverRetrieved += onePage.oppgaver.size
            logger.debug("Retrieved {} of {} oppgaver", numberOfOppgaverRetrieved, onePage.antallTreffTotalt)
        } while (numberOfOppgaverRetrieved < onePage.antallTreffTotalt)

        return OppgaveResponse(numberOfOppgaverRetrieved, allOppgaver)
    }

    fun updateOppgave(oppgaveId: Int, oppgave: EndreOppgave): Oppgave =
        oppgaveClient.putOppgave(oppgaveId, oppgave)

    fun getOppgave(oppgaveId: Int): Oppgave =
        oppgaveClient.getOppgave(oppgaveId)
}