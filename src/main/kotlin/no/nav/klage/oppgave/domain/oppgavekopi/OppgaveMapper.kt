package no.nav.klage.oppgave.domain.oppgavekopi

import no.nav.klage.oppgave.api.view.TYPE_ANKE
import no.nav.klage.oppgave.api.view.TYPE_KLAGE
import no.nav.klage.oppgave.api.view.YTELSE_FOR
import no.nav.klage.oppgave.api.view.YTELSE_SYK
import no.nav.klage.oppgave.clients.gosys.*
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import no.nav.klage.oppgave.domain.toLocalDateTime
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.clients.gosys.Prioritet as GosysPrioritet
import no.nav.klage.oppgave.clients.gosys.Status as GosysStatus
import no.nav.klage.oppgave.domain.elasticsearch.Prioritet as EsPrioritet
import no.nav.klage.oppgave.domain.elasticsearch.Status as EsStatus

class OppgaveMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun OppgaveKopi.toEsOppgave(): EsOppgave {

        return EsOppgave(
            id = id,
            versjon = versjon.toLong(),
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

    fun Oppgave.mapToOppgaveKopi(): OppgaveKopi {
        return OppgaveKopi(
            id = id,
            versjon = versjon,
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
            opprettetTidspunkt = opprettetTidspunkt.toLocalDateTime(),
            endretTidspunkt = endretTidspunkt?.toLocalDateTime(),
            ferdigstiltTidspunkt = ferdigstiltTidspunkt?.toLocalDateTime(),
            behandlesAvApplikasjon = behandlesAvApplikasjon,
            journalpostkilde = journalpostkilde,
            ident = createIdentFromIdentRelatedFields(),
            metadata = metadata?.mapToOppgaveKopi() ?: emptyMap()
        )
    }

    private fun GosysStatus.mapStatus() = Status.valueOf(name)

    private fun GosysPrioritet.mapPrioritet() = Prioritet.valueOf(name)

    private fun Map<String, String>.mapToOppgaveKopi(): Map<MetadataNoekkel, String> =
        this.map { makeMetadata(it) }.filterNotNull().toMap()

    private fun makeMetadata(it: Map.Entry<String, String>): Pair<MetadataNoekkel, String>? {
        val key: MetadataNoekkel? = mapMetadataNoekkel(it.key)
        val value = it.value
        return key?.let { Pair(it, value) }
    }

    private fun mapMetadataNoekkel(noekkel: String): MetadataNoekkel? =
        try {
            MetadataNoekkel.valueOf(noekkel)
        } catch (e: Exception) {
            logger.warn("Unable to find metadatakey ${noekkel}", e)
            null
        }

    private fun Oppgave.createIdentFromIdentRelatedFields(): Ident? {
        return if (!aktoerId.isNullOrBlank()) {
            Ident(id = null, identType = IdentType.AKTOERID, verdi = aktoerId, getFolkeregisterIdent())
        } else if (!this.orgnr.isNullOrBlank()) {
            Ident(id = null, identType = IdentType.ORGNR, verdi = orgnr)
        } else if (!this.bnr.isNullOrBlank()) {
            Ident(id = null, identType = IdentType.BNR, verdi = bnr)
        } else if (!this.samhandlernr.isNullOrBlank()) {
            Ident(id = null, identType = IdentType.SAMHANDLERNR, verdi = samhandlernr)
        } else {
            null
        }
    }

    private fun Oppgave.getFolkeregisterIdent(): String? =
        identer?.firstOrNull { it.gruppe == Gruppe.FOLKEREGISTERIDENT }?.ident

}


