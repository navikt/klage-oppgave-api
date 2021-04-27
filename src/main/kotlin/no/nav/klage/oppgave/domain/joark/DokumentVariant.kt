package no.nav.klage.oppgave.domain.joark

data class DokumentVariant(
    val filnavn: String,
    val filtype: String,
    val fysiskDokument: String,
    val variantformat: String
)