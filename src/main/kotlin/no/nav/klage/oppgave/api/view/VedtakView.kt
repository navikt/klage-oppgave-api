package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.klage.PartId
import no.nav.klage.oppgave.domain.kodeverk.Hjemmel
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import java.time.LocalDateTime
import java.util.*

data class VedtakView(
    val id: UUID,
    val utfall: Utfall? = null,
    val hjemler: MutableSet<Hjemmel> = mutableSetOf(),
    val brevMottakere: MutableSet<PartId> = mutableSetOf(),
    val finalized: LocalDateTime? = null,
    val content: String? = null
)
