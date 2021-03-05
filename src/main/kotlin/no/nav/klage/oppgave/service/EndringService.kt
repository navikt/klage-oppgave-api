package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Endring
import no.nav.klage.oppgave.domain.klage.Endringstype
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjon
import no.nav.klage.oppgave.repositories.EndringRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EndringService(
    private val endringRepository: EndringRepository,
    private val oppgaveDiffService: OppgaveDiffService
) {

    fun getEndringerNotReadBySaksbehandler(saksbehandler: String) =
        endringRepository.findBySaksbehandlerAndDatoLestIsNull(saksbehandler)

    fun checkForEndring(oppgavekopier: List<OppgaveKopiVersjon>) {
        if (oppgavekopier.size <= 1) {
            return
        }
        val endringer = oppgaveDiffService.diff(oppgavekopier[0], oppgavekopier[1])
        endringer.forEach{
            createEndring(it)
        }
    }

    fun createEndring(endring: Endring) {
        endringRepository.save(endring)
    }
}
