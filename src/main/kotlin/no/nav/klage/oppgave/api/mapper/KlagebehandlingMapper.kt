package no.nav.klage.oppgave.api.mapper


import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.api.view.KvalitetsvurderingView
import no.nav.klage.oppgave.domain.klage.Hjemmel
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.service.OppgaveKopiService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class KlagebehandlingMapper(
    private val oppgaveKopiService: OppgaveKopiService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapKlagebehandlingToKlagebehandlingView(klagebehandling: Klagebehandling): KlagebehandlingView {

        return KlagebehandlingView(
            id = klagebehandling.id,
            klageInnsendtdato = klagebehandling.innsendt,
            //TODO get name from norg2
            fraNAVEnhet = klagebehandling.mottak.avsenderEnhet ?: "mangler",
            mottattFoersteinstans = klagebehandling.mottattFoersteinstans,
            foedselsnummer = klagebehandling.foedselsnummer ?: "mangler",
            tema = klagebehandling.tema.id,
            sakstype = klagebehandling.sakstype.navn,
            mottatt = klagebehandling.mottattKlageinstans,
            startet = klagebehandling.startet,
            avsluttet = klagebehandling.avsluttet,
            frist = klagebehandling.frist,
            tildeltSaksbehandlerident = klagebehandling.tildeltSaksbehandlerident,
            hjemler = hjemmelToHjemmelView(klagebehandling.hjemler),
            modified = klagebehandling.modified,
            created = klagebehandling.created
        )
    }

    private fun hjemmelToHjemmelView(hjemler: List<Hjemmel>): List<KlagebehandlingView.Hjemmel> {
        return hjemler.map {
            KlagebehandlingView.Hjemmel(
                kapittel = it.kapittel,
                paragraf = it.paragraf,
                ledd = it.ledd,
                bokstav = it.bokstav,
                original = it.original
            )
        }
    }

    fun mapKlagebehandlingToKvalitetsvurderingView(klagebehandling: Klagebehandling): KvalitetsvurderingView {
        return KvalitetsvurderingView(
            grunn = klagebehandling.kvalitetsvurdering?.grunn?.id,
            eoes = klagebehandling.kvalitetsvurdering?.eoes?.id,
            raadfoertMedLege = klagebehandling.kvalitetsvurdering?.raadfoertMedLege?.id,
            internVurdering = klagebehandling.kvalitetsvurdering?.internVurdering,
            sendTilbakemelding = klagebehandling.kvalitetsvurdering?.sendTilbakemelding,
            tilbakemelding = klagebehandling.kvalitetsvurdering?.tilbakemelding
        )
    }
}
