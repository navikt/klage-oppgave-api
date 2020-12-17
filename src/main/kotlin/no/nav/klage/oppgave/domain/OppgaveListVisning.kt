package no.nav.klage.oppgave.domain

import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.gosys.*
import no.nav.klage.oppgave.clients.gosys.Oppgave
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface OppgaveListVisning {
    fun id(): Long
    fun versjon(): Int
    fun journalpostId(): String?
    fun saksreferanse(): String?
    fun mappeId(): Long?
    fun status(): String
    fun tildeltEnhetsnr(): String
    fun opprettetAvEnhetsnr(): String?
    fun endretAvEnhetsnr(): String?
    fun tema(): String
    fun temagruppe(): String?
    fun behandlingstema(): String?
    fun oppgavetype(): String
    fun behandlingstype(): String?
    fun prioritet(): String
    fun tilordnetRessurs(): String?
    fun beskrivelse(): String?
    fun fristFerdigstillelse(): LocalDate?
    fun aktivDato(): LocalDate
    fun opprettetAv(): String
    fun endretAv(): String?
    fun opprettetTidspunkt(): LocalDateTime
    fun endretTidspunkt(): LocalDateTime?
    fun ferdigstiltTidspunkt(): LocalDateTime?
    fun behandlesAvApplikasjon(): String?
    fun journalpostkilde(): String?
    fun aktoerId(): String?
    fun fnr(): String?
    fun hjemler(): List<String>?
    fun statuskategori(): String
    fun type(): String
    fun ytelse(): String
    fun viktigsteHjemmel(): String
}

class EsOppgaveListVisningAdapter(val esOppgave: EsOppgave) : OppgaveListVisning {

    override fun id(): Long = esOppgave.id

    override fun versjon(): Int = esOppgave.versjon!!.toInt()

    override fun journalpostId(): String? = esOppgave.journalpostId

    override fun saksreferanse(): String? = esOppgave.saksreferanse

    override fun mappeId(): Long? = esOppgave.mappeId

    override fun status(): String = esOppgave.status.name

    override fun tildeltEnhetsnr(): String = esOppgave.tildeltEnhetsnr

    override fun opprettetAvEnhetsnr(): String? = esOppgave.opprettetAvEnhetsnr

    override fun endretAvEnhetsnr(): String? = esOppgave.endretAvEnhetsnr

    override fun tema(): String = esOppgave.tema

    override fun temagruppe(): String? = esOppgave.temagruppe

    override fun behandlingstema(): String? = esOppgave.behandlingstema

    override fun oppgavetype(): String = esOppgave.oppgavetype

    override fun behandlingstype(): String? = esOppgave.behandlingstype

    override fun prioritet(): String = esOppgave.prioritet.name

    override fun tilordnetRessurs(): String? = esOppgave.tilordnetRessurs

    override fun beskrivelse(): String? = esOppgave.beskrivelse

    override fun fristFerdigstillelse(): LocalDate? = esOppgave.fristFerdigstillelse

    override fun aktivDato(): LocalDate = esOppgave.aktivDato

    override fun opprettetAv(): String = esOppgave.opprettetAv

    override fun endretAv(): String? = esOppgave.endretAv

    override fun opprettetTidspunkt(): LocalDateTime = esOppgave.opprettetTidspunkt

    override fun endretTidspunkt(): LocalDateTime? = esOppgave.endretTidspunkt

    override fun ferdigstiltTidspunkt(): LocalDateTime? = esOppgave.ferdigstiltTidspunkt

    override fun behandlesAvApplikasjon(): String? = esOppgave.behandlesAvApplikasjon

    override fun journalpostkilde(): String? = esOppgave.journalpostkilde

    override fun aktoerId(): String? = esOppgave.aktoerId

    override fun fnr(): String? = esOppgave.fnr

    override fun hjemler(): List<String>? = esOppgave.hjemler

    override fun statuskategori(): String = esOppgave.statuskategori.name

    override fun type(): String = esOppgave.statuskategori.name

    override fun ytelse(): String = esOppgave.statuskategori.name

    override fun viktigsteHjemmel(): String = esOppgave.hjemler?.firstOrNull() ?: "mangler"
}

class GosysOppgaveListVisningAdapter(val oppgave: Oppgave) : OppgaveListVisning {

    override fun id(): Long = oppgave.id

    override fun versjon(): Int = oppgave.versjon

    override fun journalpostId(): String? = oppgave.journalpostId

    override fun saksreferanse(): String? = oppgave.saksreferanse

    override fun mappeId(): Long? = oppgave.mappeId

    override fun status(): String = oppgave.status.name

    override fun tildeltEnhetsnr(): String = oppgave.tildeltEnhetsnr

    override fun opprettetAvEnhetsnr(): String? = oppgave.opprettetAvEnhetsnr

    override fun endretAvEnhetsnr(): String? = oppgave.endretAvEnhetsnr

    override fun tema(): String = oppgave.tema

    override fun temagruppe(): String? = oppgave.temagruppe

    override fun behandlingstema(): String? = oppgave.behandlingstema

    override fun oppgavetype(): String = oppgave.oppgavetype

    override fun behandlingstype(): String? = oppgave.behandlingstype

    override fun prioritet(): String = oppgave.prioritet.name

    override fun tilordnetRessurs(): String? = oppgave.tilordnetRessurs

    override fun beskrivelse(): String? = oppgave.beskrivelse

    override fun fristFerdigstillelse(): LocalDate? = oppgave.fristFerdigstillelse

    override fun aktivDato(): LocalDate = oppgave.aktivDato

    override fun opprettetAv(): String = oppgave.opprettetAv

    override fun endretAv(): String? = oppgave.endretAv

    override fun opprettetTidspunkt(): LocalDateTime = oppgave.opprettetTidspunkt.toLocalDateTime()

    override fun endretTidspunkt(): LocalDateTime? = oppgave.endretTidspunkt?.toLocalDateTime()

    override fun ferdigstiltTidspunkt(): LocalDateTime? = oppgave.ferdigstiltTidspunkt?.toLocalDateTime()

    override fun behandlesAvApplikasjon(): String? = oppgave.behandlesAvApplikasjon

    override fun journalpostkilde(): String? = oppgave.journalpostkilde

    override fun aktoerId(): String? = oppgave.aktoerId

    override fun fnr(): String? = getFnrForBruker()

    override fun hjemler(): List<String> = listOf(viktigsteHjemmel())

    override fun statuskategori(): String = oppgave.status.statuskategori().name

    override fun type(): String = toType()

    override fun ytelse(): String = toYtelse()

    override fun viktigsteHjemmel(): String = toHjemmel()

    private fun toHjemmel() = oppgave.metadata?.get(HJEMMEL) ?: "mangler"

    private fun toType(): String {
        return if (oppgave.behandlingstema == null) {
            when (oppgave.behandlingstype) {
                BEHANDLINGSTYPE_KLAGE -> TYPE_KLAGE
                BEHANDLINGSTYPE_ANKE -> TYPE_ANKE
                else -> "ukjent"
            }
        } else "mangler"
    }

    private fun toYtelse(): String = when (oppgave.tema) {
        TEMA_SYK -> YTELSE_SYK
        TEMA_FOR -> YTELSE_FOR
        else -> oppgave.tema
    }

    private fun getFnrForBruker() = oppgave.identer?.find { i -> i.gruppe == Gruppe.FOLKEREGISTERIDENT }?.ident
}

//TODO: Det kan være at denne ikke håndterer tidssonene riktig..
//Se https://github.com/navikt/oppgave/blob/e7d1b3a4af7b3b42579f69362ebfaebbe8a6dacc/src/main/java/no/nav/oppgave/util/TimUt.java#L6
fun Date.toLocalDateTime(): LocalDateTime = java.sql.Timestamp(time).toLocalDateTime()



