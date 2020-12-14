package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjon
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjonId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OppgaveKopiVersjonRepository : JpaRepository<OppgaveKopiVersjon, OppgaveKopiVersjonId> {

    fun findFirstByIdOrderByVersjonDesc(oppgaveKopiId: Long): OppgaveKopiVersjon
}