package no.nav.klage.oppgave.domain

data class OppgaverQueryParams(
    var typer: List<String> = emptyList(),
    var ytelser: List<String> = emptyList(),
    var hjemler: List<String> = emptyList(),
    val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    val start: Int,
    val antall: Int,
    val projeksjon: Projeksjon? = null,
    val tildeltSaksbehandler: String? = null
) {
    enum class Rekkefoelge {
        STIGENDE, SYNKENDE
    }

    enum class Projeksjon {
        UTVIDET
    }
}
