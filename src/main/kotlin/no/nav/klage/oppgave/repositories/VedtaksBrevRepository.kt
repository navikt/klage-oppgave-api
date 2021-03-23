package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.vedtaksbrev.VedtaksBrev
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VedtaksBrevRepository : JpaRepository<VedtaksBrev, UUID> {
    fun findByKlagebehandlingId(klagebehandlingId: UUID): List<VedtaksBrev>
}
