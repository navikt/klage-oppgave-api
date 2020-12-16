package no.nav.klage.oppgave.api.view

data class Saksbehandlertildeling(
    val navIdent: String,
    val oppgaveversjon: String
)

data class Saksbehandlerfradeling(
    val oppgaveversjon: String
)