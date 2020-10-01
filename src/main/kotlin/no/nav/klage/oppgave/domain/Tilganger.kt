package no.nav.klage.oppgave.domain

data class Tilganger(
    val enheter: Array<Enhet>
)

data class Enhet(val enhetId: String, val fagomrader: Array<String>, val navn: String)