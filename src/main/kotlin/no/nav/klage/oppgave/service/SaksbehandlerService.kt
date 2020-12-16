package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.axsys.Tilganger
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) {
    fun getTilgangerForSaksbehandler(): Tilganger =
        innloggetSaksbehandlerRepository.getTilgangerForSaksbehandler()
}