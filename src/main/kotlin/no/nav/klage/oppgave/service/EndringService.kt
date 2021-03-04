package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Endring
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjon
import no.nav.klage.oppgave.repositories.EndringRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EndringService(
    private val endringRepository: EndringRepository,
    private val tokenService: TokenService
) {

    fun getEndringerNotReadBySaksbehandler(saksbehandler: String) =
        endringRepository.findBySaksbehadlerAndDatoLestIsNull(saksbehandler)

    fun checkForEndring(oppgavekopier: List<OppgaveKopiVersjon>) {
        if (oppgavekopier.size <= 1) {
            return
        }
        val endringer = diff(oppgavekopier[0], oppgavekopier[1])
        endringer.forEach{
            createEndring(it)
        }
    }

    fun createEndring(endring: Endring) {
        endringRepository.save(endring)
    }

    // TODO flytte til et mer egnet sted...
    // TODO dette er ikke en smart måte å diffe på, men det er en POC for å vise tankegang :)
    // TODO bør også ha en kobling til klagebehandling
    private fun diff(nyeste: OppgaveKopiVersjon, nestNyeste: OppgaveKopiVersjon): List<Endring> {
        val endringsliste = mutableListOf<Endring>()
        if (nyeste.tilordnetRessurs != nestNyeste.tilordnetRessurs) {
            endringsliste.add(
                Endring(
                    saksbehadler = tokenService.getIdent(),
                    type = "VARSEL",
                    melding = "Saksbehandler på oppgave endret til ${nyeste.tilordnetRessurs}"
            ))
        }
        return endringsliste
    }
}
