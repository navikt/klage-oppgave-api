package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.klage.Endringstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class EndringView(
    val id: UUID,
    val saksbehandler: String,
    val type: Endringstype,
    val melding: String,
    val behandlingSkygge: BehandlingSkyggeView,
    val created: LocalDateTime = LocalDateTime.now()
)

data class BehandlingSkyggeView(
    val id: UUID,
    val hjemmel: String,
    val frist: LocalDate?,
    val tema: Tema
)
