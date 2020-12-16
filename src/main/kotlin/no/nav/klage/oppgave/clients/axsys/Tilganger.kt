package no.nav.klage.oppgave.clients.axsys

data class Tilganger(val enheter: List<Enhet>)

data class Enhet(val enhetId: String, val fagomrader: List<String>, val navn: String)

data class Bruker(val appIdent: String, val historiskIdent: Long)