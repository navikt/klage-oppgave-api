package no.nav.klage.oppgave.domain.joark

data class AvsenderMottaker(
    val id: String,
    val idType: AvsenderMottakerIdType,
    val navn: String? = null,
    val land: String? = null
)