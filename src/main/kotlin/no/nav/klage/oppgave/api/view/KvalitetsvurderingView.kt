package no.nav.klage.oppgave.api.view

data class KvalitetsvurderingView(
    val grunn: Int?,
    val eoes: Int?,
    val raadfoertMedLege: Int?,
    val internVurdering: String?,
    val sendTilbakemelding: Boolean?,
    val tilbakemelding: String?
)
