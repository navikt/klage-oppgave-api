package no.nav.klage.oppgave.api.view

import java.time.LocalDateTime

data class VedleggView(
    val name: String,
    val size: Long,
    val opplastet: LocalDateTime
)