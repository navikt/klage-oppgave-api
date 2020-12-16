package no.nav.klage.oppgave.clients.gosys

import java.time.LocalDate

const val BEHANDLINGSTYPE_KLAGE = "ae0058"
const val BEHANDLINGSTYPE_ANKE = "ae0046"

const val TEMA_SYK = "SYK"
const val TEMA_FOR = "FOR"

data class OppgaveResponse(
    val antallTreffTotalt: Int,
    val oppgaver: List<Oppgave>
)

data class Oppgave(
    val id: Long,
    val tildeltEnhetsnr: String? = null,
    val endretAvEnhetsnr: String? = null,
    val opprettetAvEnhetsnr: String? = null,
    val journalpostId: String? = null,
    val journalpostkilde: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val bnr: String? = null,
    val samhandlernr: String? = null,
    val aktoerId: String? = null,
    val identer: List<Ident>? = null,
    val orgnr: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val temagruppe: String? = null,
    val tema: String,
    val behandlingstema: String? = null,
    val oppgavetype: String? = null,
    val behandlingstype: String? = null,
    val versjon: Int,
    val mappeId: Long? = null,
    val opprettetAv: String? = null,
    val endretAv: String? = null,
    val prioritet: Prioritet? = null,
    val status: Status? = null,
    val metadata: Map<String, String>? = null,
    val fristFerdigstillelse: LocalDate?,
    val aktivDato: String? = null,
    val opprettetTidspunkt: String? = null,
    val ferdigstiltTidspunkt: String? = null,
    val endretTidspunkt: String? = null
) {
    fun toEndreOppgave() = EndreOppgave(
        id = id,
        tildeltEnhetsnr = tildeltEnhetsnr,
        endretAvEnhetsnr = endretAvEnhetsnr,
        journalpostId = journalpostId,
        journalpostkilde = journalpostkilde,
        behandlesAvApplikasjon = behandlesAvApplikasjon,
        saksreferanse = saksreferanse,
        bnr = bnr,
        samhandlernr = samhandlernr,
        aktoerId = aktoerId,
        orgnr = orgnr,
        tilordnetRessurs = tilordnetRessurs,
        beskrivelse = beskrivelse,
        temagruppe = temagruppe,
        tema = tema,
        behandlingstema = behandlingstema,
        oppgavetype = oppgavetype,
        behandlingstype = behandlingstype,
        versjon = versjon,
        mappeId = mappeId,
        prioritet = prioritet,
        status = status,
        metadata = metadata?.toMutableMap(),
        fristFerdigstillelse = fristFerdigstillelse,
        aktivDato = aktivDato
    )

}

// De feltene som skal kunne endres må gjøres mutable/defineres som var isf val
data class EndreOppgave(
    val id: Long,
    val tildeltEnhetsnr: String? = null,
    val endretAvEnhetsnr: String? = null,
    val journalpostId: String? = null,
    val journalpostkilde: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val bnr: String? = null,
    val samhandlernr: String? = null,
    val aktoerId: String? = null,
    val orgnr: String? = null,
    var tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val temagruppe: String? = null,
    val tema: String,
    val behandlingstema: String? = null,
    val oppgavetype: String? = null,
    val behandlingstype: String? = null,
    var versjon: Int? = null,
    val mappeId: Long? = null,
    val prioritet: Prioritet? = null,
    val status: Status? = null,
    var metadata: MutableMap<String, String>? = null,
    val fristFerdigstillelse: LocalDate?,
    val aktivDato: String? = null
)

data class Ident(
    val ident: String? = null,
    val gruppe: Gruppe? = null
)

enum class Gruppe {
    FOLKEREGISTERIDENT, AKTOERID, NPID
}

enum class Prioritet {
    HOY, NORM, LAV
}

enum class Status {
    OPPRETTET, AAPNET, UNDER_BEHANDLING, FERDIGSTILT, FEILREGISTRERT
}


val hjemler = listOf(
    "8-1",
    "8-2",
    "8-3",
    "8-4",
    "8-5",
    "8-6",
    "8-7",
    "8-8",
    "8-9",
    "8-10",
    "8-11",
    "8-12",
    "8-13",
    "8-14",
    "8-15",
    "8-16",
    "8-17",
    "8-18",
    "8-19",
    "8-20",
    "8-21",
    "8-22",
    "8-23",
    "8-24",
    "8-25",
    "8-26",
    "8-27",
    "8-28",
    "8-29",
    "8-30",
    "8-31",
    "8-32",
    "8-33",
    "8-34",
    "8-35",
    "8-36",
    "8-37",
    "8-38",
    "8-39",
    "8-40",
    "8-41",
    "8-42",
    "8-43",
    "8-44",
    "8-45",
    "8-46",
    "8-47",
    "8-48",
    "8-49",
    "8-50",
    "8-51",
    "8-52",
    "8-53",
    "8-54",
    "8-55",
    "21-7",
    "21-12",
    "22-3",
    "22-13"
)
