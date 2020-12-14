package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopi
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OppgaveKopiRepository : JpaRepository<OppgaveKopi, Long> {

}