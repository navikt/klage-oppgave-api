package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.*
import no.nav.klage.oppgave.events.KlagebehandlingEndretEvent
import no.nav.klage.oppgave.exceptions.VedtakNotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object KlagebehandlingAggregatFunctions {

    fun Klagebehandling.setTildeltSaksbehandlerident(
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = tildeltSaksbehandlerident
        val tidspunkt = LocalDateTime.now()
        tildeltSaksbehandlerident = nyVerdi
        modified = tidspunkt

        val endringslogginnslag = mutableListOf<Endringslogginnslag>()
        if (startet == null) {
            startet = tidspunkt.toLocalDate()
            endringslogg(
                saksbehandlerident,
                Felt.STARTET,
                null,
                startet?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                tidspunkt
            )?.let { endringslogginnslag.add(it) }
        }

        endringslogg(
            saksbehandlerident,
            Felt.TILDELT_SAKSBEHANDLERIDENT,
            gammelVerdi,
            nyVerdi,
            tidspunkt
        )?.let { endringslogginnslag.add(it) }
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = endringslogginnslag)
    }

    fun Klagebehandling.setTildeltEnhet(
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = tildeltEnhet
        val tidspunkt = LocalDateTime.now()
        tildeltEnhet = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(saksbehandlerident, Felt.TILDELT_ENHET, gammelVerdi, nyVerdi, tidspunkt)
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setSakstype(
        nyVerdi: Type,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = type
        val tidspunkt = LocalDateTime.now()
        type = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(saksbehandlerident, Felt.SAKSTYPE, gammelVerdi.id.toString(), nyVerdi.id.toString(), tidspunkt)
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setTema(
        nyVerdi: Tema,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = tema
        val tidspunkt = LocalDateTime.now()
        tema = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(saksbehandlerident, Felt.TEMA, gammelVerdi.id.toString(), nyVerdi.id.toString(), tidspunkt)
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setInnsendt(
        nyVerdi: LocalDate,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = innsendt
        val tidspunkt = LocalDateTime.now()
        innsendt = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.DATO_KLAGE_INNSENDT,
                gammelVerdi?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                nyVerdi.format(DateTimeFormatter.ISO_LOCAL_DATE),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setMottattFoersteinstans(
        nyVerdi: LocalDate,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = mottattFoersteinstans
        val tidspunkt = LocalDateTime.now()
        mottattFoersteinstans = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.DATO_MOTTATT_FOERSTEINSTANS,
                gammelVerdi?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                nyVerdi.format(DateTimeFormatter.ISO_LOCAL_DATE),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setMottattKlageinstans(
        nyVerdi: LocalDate,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = mottattKlageinstans
        val tidspunkt = LocalDateTime.now()
        mottattKlageinstans = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.DATO_OVERSENDT_KA,
                gammelVerdi.format(DateTimeFormatter.ISO_LOCAL_DATE),
                nyVerdi.format(DateTimeFormatter.ISO_LOCAL_DATE),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setFrist(
        nyVerdi: LocalDate,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = frist
        val tidspunkt = LocalDateTime.now()
        frist = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.DATO_FRIST,
                gammelVerdi?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                nyVerdi.format(DateTimeFormatter.ISO_LOCAL_DATE),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setAvsenderSaksbehandleridentFoersteinstans(
        nyVerdi: String,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = avsenderSaksbehandleridentFoersteinstans
        val tidspunkt = LocalDateTime.now()
        avsenderSaksbehandleridentFoersteinstans = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.AVSENDER_SAKSBEHANDLERIDENT,
                gammelVerdi,
                nyVerdi,
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setAvsenderEnhetFoersteinstans(
        nyVerdi: String,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = avsenderEnhetFoersteinstans
        val tidspunkt = LocalDateTime.now()
        avsenderEnhetFoersteinstans = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.AVSENDER_ENHET,
                gammelVerdi,
                nyVerdi,
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setMedunderskriverident(
        nyVerdi: String,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = medunderskriverident
        val tidspunkt = LocalDateTime.now()
        medunderskriverident = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.MEDUNDERSKRIVERIDENT,
                gammelVerdi,
                nyVerdi,
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingGrunn(
        nyVerdi: Grunn?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.grunn
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.grunn = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.TILDELT_SAKSBEHANDLERIDENT,
                gammelVerdi?.id.toString(),
                nyVerdi?.id.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingEoes(
        nyVerdi: Eoes?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.eoes
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.eoes = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.TILDELT_SAKSBEHANDLERIDENT,
                gammelVerdi?.id.toString(),
                nyVerdi?.id.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingRaadfoertMedLege(
        nyVerdi: RaadfoertMedLege?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.raadfoertMedLege
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.raadfoertMedLege = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.TILDELT_SAKSBEHANDLERIDENT,
                gammelVerdi?.id.toString(),
                nyVerdi?.id.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingInternvurdering(
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.internVurdering
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.internVurdering = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(saksbehandlerident, Felt.TILDELT_SAKSBEHANDLERIDENT, gammelVerdi, nyVerdi, tidspunkt)
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingSendTilbakemelding(
        nyVerdi: Boolean?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.sendTilbakemelding
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.sendTilbakemelding = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.TILDELT_SAKSBEHANDLERIDENT,
                gammelVerdi?.toString(),
                nyVerdi?.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingTilbakemelding(
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.tilbakemelding
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.tilbakemelding = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(saksbehandlerident, Felt.TILDELT_SAKSBEHANDLERIDENT, gammelVerdi, nyVerdi, tidspunkt)
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setUtfallInVedtak(
        vedtakId: UUID,
        nyVerdi: Utfall,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent? {
        val vedtak = getVedtakFromKlagebehandling(this, vedtakId)
        val gammelVerdi = vedtak.utfall
        val tidspunkt = LocalDateTime.now()
        vedtak.utfall = nyVerdi
        vedtak.modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.TILDELT_SAKSBEHANDLERIDENT,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.addSaksdokument(
        saksdokument: Saksdokument,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent? {
        if (saksdokumenter.none { it.journalpostId == saksdokument.journalpostId }) {
            val tidspunkt = LocalDateTime.now()
            saksdokumenter.add(saksdokument)
            val endringslogg = Endringslogginnslag.endringslogg(
                saksbehandlerident,
                Felt.SAKSDOKUMENT,
                null,
                saksdokument.journalpostId,
                id,
                tidspunkt
            )
            return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
        }
        return null
    }

    fun Klagebehandling.removeSaksdokument(
        saksdokument: Saksdokument,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent? {
        if (saksdokumenter.any { it.journalpostId == saksdokument.journalpostId && it.dokumentInfoId == saksdokument.dokumentInfoId }) {
            val tidspunkt = LocalDateTime.now()
            saksdokumenter.removeIf { it.journalpostId == saksdokument.journalpostId }
            val endringslogg = Endringslogginnslag.endringslogg(
                saksbehandlerident,
                Felt.SAKSDOKUMENT,
                saksdokument.journalpostId,
                null,
                id,
                tidspunkt
            )
            return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
        }
        return null
    }

    private fun Klagebehandling.endringslogg(
        saksbehandlerident: String,
        felt: Felt,
        fraVerdi: String?,
        tilVerdi: String?,
        tidspunkt: LocalDateTime
    ): Endringslogginnslag? {
        return Endringslogginnslag.endringslogg(
            saksbehandlerident,
            felt,
            fraVerdi,
            tilVerdi,
            this.id,
            tidspunkt
        )
    }

    private fun getVedtakFromKlagebehandling(klagebehandling: Klagebehandling, vedtakId: UUID): Vedtak {
        return klagebehandling.vedtak.firstOrNull() {
            it.id == vedtakId
        } ?: throw VedtakNotFoundException("Vedtak med id $vedtakId ikke funnet")
    }
}