package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.klage.Endringstype
import java.time.LocalDateTime
import java.util.*

data class EndringView(
    val id: UUID,
    val saksbehandler: String,
    val type: Endringstype,
    val melding: String,
    val created: LocalDateTime = LocalDateTime.now()
)
