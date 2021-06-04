package no.nav.klage.oppgave.domain.joark

data class FerdigstillJournalpostPayload(
    val journalfoerendeEnhet: String
)

data class AvbrytJournalpostPayload(
    val journalpostId: String
)