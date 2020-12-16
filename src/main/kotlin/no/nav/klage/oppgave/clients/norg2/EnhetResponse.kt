package no.nav.klage.oppgave.clients.norg2

data class EnhetResponse(
    val antallRessurser: Int,
    val enhetId: Int,
    val enhetNr: String,
    val kanalstrategi: String,
    val navn: String,
    val oppgavebehandler: Boolean,
    val sosialeTjenester: String,
    val status: String,
    val type: String
) {
    fun asEnhet() = Enhet(navn)
}
