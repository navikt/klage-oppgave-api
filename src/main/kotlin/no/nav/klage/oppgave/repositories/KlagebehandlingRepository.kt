package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Klagebehandling
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface KlagebehandlingRepository : JpaRepository<Klagebehandling, UUID> {

    fun findByOppgavereferanserOppgaveId(oppgaveId: Long): Klagebehandling?
}