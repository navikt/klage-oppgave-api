package no.nav.klage.oppgave.domain.oppgavekopi.helper

import no.nav.klage.oppgave.api.view.TYPE_ANKE
import no.nav.klage.oppgave.api.view.TYPE_KLAGE
import no.nav.klage.oppgave.api.view.YTELSE_FOR
import no.nav.klage.oppgave.api.view.YTELSE_SYK
import no.nav.klage.oppgave.clients.gosys.BEHANDLINGSTYPE_ANKE
import no.nav.klage.oppgave.clients.gosys.BEHANDLINGSTYPE_KLAGE
import no.nav.klage.oppgave.clients.gosys.TEMA_FOR
import no.nav.klage.oppgave.clients.gosys.TEMA_SYK
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import no.nav.klage.oppgave.domain.oppgavekopi.*
import no.nav.klage.oppgave.domain.elasticsearch.Prioritet as EsPrioritet
import no.nav.klage.oppgave.domain.elasticsearch.Status as EsStatus

fun OppgaveKopi.toEsOppgave(): EsOppgave {

    return EsOppgave(
        id = id,
        version = versjon.toLong(),
        journalpostId = journalpostId,
        saksreferanse = saksreferanse,
        mappeId = mappeId,
        status = status.mapStatus(),
        tildeltEnhetsnr = tildeltEnhetsnr,
        opprettetAvEnhetsnr = opprettetAvEnhetsnr,
        endretAvEnhetsnr = endretAvEnhetsnr,
        tema = tema,
        temagruppe = temagruppe,
        behandlingstema = behandlingstema,
        oppgavetype = oppgavetype,
        behandlingstype = behandlingstype,
        prioritet = prioritet.mapPrioritet(),
        tilordnetRessurs = tilordnetRessurs,
        beskrivelse = beskrivelse,
        fristFerdigstillelse = fristFerdigstillelse,
        aktivDato = aktivDato,
        opprettetAv = opprettetAv,
        endretAv = endretAv,
        opprettetTidspunkt = opprettetTidspunkt,
        endretTidspunkt = endretTidspunkt,
        ferdigstiltTidspunkt = ferdigstiltTidspunkt,
        behandlesAvApplikasjon = behandlesAvApplikasjon,
        journalpostkilde = journalpostkilde,
        aktoerId = ident.aktorIdFromIdent(),
        fnr = ident.fnrFromIdent(),
        hjemler = listOfNotNull(metadataAsMap()[MetadataNoekkel.HJEMMEL]),
        egenAnsatt = false,
        type = mapType(),
        ytelse = mapYtelse()
    )
}

private fun OppgaveKopi.mapType(): String {
    return if (behandlingstema == null) {
        when (behandlingstype) {
            BEHANDLINGSTYPE_KLAGE -> TYPE_KLAGE
            BEHANDLINGSTYPE_ANKE -> TYPE_ANKE
            else -> "ukjent"
        }
    } else "mangler"
}

private fun OppgaveKopi.mapYtelse(): String = when (tema) {
    TEMA_SYK -> YTELSE_SYK
    TEMA_FOR -> YTELSE_FOR
    else -> tema
}

private fun Status.mapStatus() = EsStatus.valueOf(name)

private fun Prioritet.mapPrioritet() = EsPrioritet.valueOf(name)

fun Ident?.fnrFromIdent(): String? =
    if (this != null && this.identType == IdentType.AKTOERID) {
        this.folkeregisterident
    } else {
        null
    }

fun Ident?.aktorIdFromIdent(): String? =
    if (this != null && this.identType == IdentType.AKTOERID) {
        this.verdi
    } else {
        null
    }


