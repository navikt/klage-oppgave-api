package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Endring
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EndringRepository : JpaRepository<Endring, UUID> {
    fun findBySaksbehadlerAndDatoLestIsNull(saksbehandler: String): List<Endring>
}
