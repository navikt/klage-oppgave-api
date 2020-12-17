package no.nav.klage.oppgave.domain.oppgavekopi.helper

import no.nav.klage.oppgave.clients.gosys.Gruppe
import no.nav.klage.oppgave.clients.gosys.Oppgave
import no.nav.klage.oppgave.domain.oppgavekopi.Ident
import no.nav.klage.oppgave.domain.oppgavekopi.IdentType
import no.nav.klage.oppgave.domain.oppgavekopi.MetadataNoekkel
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopi
import no.nav.klage.oppgave.domain.oppgavekopi.Prioritet
import no.nav.klage.oppgave.domain.oppgavekopi.Status
import no.nav.klage.oppgave.util.getLogger
import java.time.LocalDateTime
import java.util.*
import no.nav.klage.oppgave.clients.gosys.Prioritet as GosysPrioritet
import no.nav.klage.oppgave.clients.gosys.Status as GosysStatus

class OppgaveMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
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

//TODO: Mulig denne ikke håndterer tidssonene riktig, ref koden til Oppgave
private fun Date.toLocalDateTime(): LocalDateTime = java.sql.Timestamp(time).toLocalDateTime()


