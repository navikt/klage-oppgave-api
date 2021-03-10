package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.BehandlingSkygge
import no.nav.klage.oppgave.domain.klage.Endring
import no.nav.klage.oppgave.domain.klage.Endringstype
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjon
import no.nav.klage.oppgave.repositories.EndringRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EndringService(
    private val tokenService: TokenService,
    private val klagebehandlingService: KlagebehandlingService,
    private val endringRepository: EndringRepository,
    private val oppgaveDiffService: OppgaveDiffService
) {

    fun getEndringerNotReadBySaksbehandler(saksbehandler: String) =
        endringRepository.findBySaksbehandlerAndDatoLestIsNull(saksbehandler)

    fun checkForEndring(oppgavekopier: List<OppgaveKopiVersjon>) {
        if (oppgavekopier.size <= 1) {
            return
        }
        val endring = oppgaveDiffService.diff(oppgavekopier[0], oppgavekopier[1])
        if(endring != null) {
            val klagebehandling = klagebehandlingService.getKlagebehandlingForOppgaveId(oppgavekopier[0].id).first()
            saveEndring(
                Endring(
                    saksbehandler = tokenService.getIdent(),
                    type = endring.second,
                    melding = endring.first,
                    behandlingSkygge = BehandlingSkygge(
                        hjemmel = "",
                        frist = klagebehandling.frist,
                        tema = klagebehandling.tema
                    )
                )
            )
        }
    }

    private fun saveEndring(endring: Endring) {
        endringRepository.save(endring)
    }
}
