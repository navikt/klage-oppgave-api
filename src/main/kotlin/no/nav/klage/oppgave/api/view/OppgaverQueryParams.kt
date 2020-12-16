package no.nav.klage.oppgave.api.view

data class OppgaverQueryParams(
    var typer: List<String> = emptyList(),
    var ytelser: List<String> = emptyList(),
    var hjemler: List<String> = emptyList(),
    val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    val start: Int,
    val antall: Int,
    val projeksjon: Projeksjon? = null,
    val erTildeltSaksbehandler: Boolean? = null,
    val tildeltSaksbehandler: String? = null,
    val enhetId: String
) {

    enum class Rekkefoelge {
        STIGENDE, SYNKENDE
    }

    enum class Projeksjon {
        UTVIDET
    }
}
