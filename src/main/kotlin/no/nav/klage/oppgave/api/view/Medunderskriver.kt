package no.nav.klage.oppgave.api.view

data class Medunderskrivere(val tema: String, val medunderskrivere: List<Medunderskriver>)

data class Medunderskriver(val ident: String, val navn: String)
