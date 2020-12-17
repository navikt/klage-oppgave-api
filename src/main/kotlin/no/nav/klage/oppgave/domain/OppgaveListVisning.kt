package no.nav.klage.oppgave.domain

import java.time.LocalDate

interface OppgaveListVisning {
    val id: Long
    val versjon: Int
    val statusString: String
    val tildeltEnhetsnr: String
    val tilordnetRessurs: String?
    val beskrivelse: String?
    val fristFerdigstillelse: LocalDate?
    val fnr: String?
    val hjemler: List<String>?
    val type: String
    val ytelse: String
    val viktigsteHjemmel: String
}


