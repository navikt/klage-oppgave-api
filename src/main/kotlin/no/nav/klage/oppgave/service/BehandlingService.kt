package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Behandling
import no.nav.klage.oppgave.repositories.BehandlingRepository
import no.nav.klage.oppgave.repositories.KlagesakRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BehandlingService(
    private val klagesakRepository: KlagesakRepository,
    private val behandlingRepository: BehandlingRepository
) {

    fun insertNewBehandling(behandling: Behandling) {
        if (!klagesakRepository.existsById(behandling.klagesak.id)) {
            klagesakRepository.save(behandling.klagesak)
        }
        behandlingRepository.save(behandling)
    }

}
