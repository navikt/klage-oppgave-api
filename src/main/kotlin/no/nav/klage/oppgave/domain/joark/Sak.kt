package no.nav.klage.oppgave.domain.joark

data class Sak(
    val sakstype: Sakstype,
    val fagsaksystem: FagsaksSystem? = null,
    val fagsakid: String? = null,
    val arkivsaksystem: ArkivsaksSystem? = null,
    val arkivsaksnummer: String? = null
)