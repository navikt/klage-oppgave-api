package no.nav.klage.oppgave.api.view

import java.time.LocalDate

const val HJEMMEL = "HJEMMEL"
const val TYPE_KLAGE = "Klage"
const val TYPE_ANKE = "Anke"
const val YTELSE_SYK = "Sykepenger"
const val YTELSE_FOR = "Foreldrepenger"

data class OppgaverRespons(
    val antallTreffTotalt: Int,
    val oppgaver: List<Oppgave>
)

data class Oppgave(
    val id: String,
    val person: Person? = null,
    val type: String,
    val ytelse: String,
    val hjemmel: String?,
    val frist: LocalDate?,
    val versjon: Int
) {
    data class Person(
        val fnr: String,
        val navn: String
    )
}