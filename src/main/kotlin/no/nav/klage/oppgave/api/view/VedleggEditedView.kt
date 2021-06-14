package no.nav.klage.oppgave.api.view

import java.time.LocalDateTime

data class VedleggEditedView(
    val klagebehandlingVersjon: Long,
    val modified: LocalDateTime,
    val file: VedleggView?
)
