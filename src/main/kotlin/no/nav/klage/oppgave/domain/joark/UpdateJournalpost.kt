package no.nav.klage.oppgave.domain.joark

import no.nav.klage.oppgave.domain.kodeverk.Tema

data class UpdateJournalpost(
    val tema: Tema,
    val behandlingstema: String,
    val kanal: String = "NAV_NO",
    val tittel: String,
    val avsenderMottaker: AvsenderMottaker? = null,
    val journalfoerendeEnhet: String? = null,
    val eksternReferanseId: String? = null,
    val bruker: Bruker? = null,
    val sak: Sak? = null,
    val dokumenter: List<Dokument>? = mutableListOf(),
    val tilleggsopplysninger: List<Tilleggsopplysning> = mutableListOf()
)