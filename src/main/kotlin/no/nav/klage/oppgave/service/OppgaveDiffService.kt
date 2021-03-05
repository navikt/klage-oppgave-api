package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Endring
import no.nav.klage.oppgave.domain.klage.Endringstype
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjon
import org.springframework.stereotype.Service

@Service
class OppgaveDiffService(
    private val tokenService: TokenService
) {
    fun diff(nyeste: OppgaveKopiVersjon, nestNyeste: OppgaveKopiVersjon): List<Endring> {
        val endringsliste = mutableListOf<Endring>()
        if (nyeste harEndretSaksbehandlerFra nestNyeste) {
            endringsliste.add(
                Endring(
                    saksbehandler = tokenService.getIdent(),
                    type = Endringstype.VARSEL,
                    melding = "Saksbehandler på oppgave endret til ${nyeste.tilordnetRessurs}"
                )
            )
        }
        if (nyeste harEndretEnhetTilAnnenKaEnhetFra nestNyeste) {
            endringsliste.add(
                Endring(
                    saksbehandler = tokenService.getIdent(),
                    type = Endringstype.VARSEL,
                    melding = "Enhet på oppgave endret til ${nyeste.tildeltEnhetsnr}"
                )
            )
        }
        if (nyeste harEndretEnhetTilIkkeKaEnhetFra nestNyeste) {
            endringsliste.add(
                Endring(
                    saksbehandler = tokenService.getIdent(),
                    type = Endringstype.FEIL,
                    melding = "Enhet på oppgave endret til ${nyeste.tildeltEnhetsnr}"
                )
            )
        }
        return endringsliste
    }

    private infix fun OppgaveKopiVersjon.harEndretEnhetTilIkkeKaEnhetFra(other: OppgaveKopiVersjon): Boolean {
        if (this.tildeltEnhetsnr != other.tildeltEnhetsnr) {
            if (!this.tildeltEnhetsnr.isKaEnhet()) {
                return true
            }
        }
        return false
    }

    private infix fun OppgaveKopiVersjon.harEndretEnhetTilAnnenKaEnhetFra(other: OppgaveKopiVersjon): Boolean {
        if (this.tildeltEnhetsnr != other.tildeltEnhetsnr) {
            if (this.tildeltEnhetsnr.isKaEnhet()) {
                return true
            }
        }
        return false
    }

    private infix fun OppgaveKopiVersjon.harEndretSaksbehandlerFra(other: OppgaveKopiVersjon): Boolean {
        if (this.tilordnetRessurs != other.tilordnetRessurs) {
            return true
        }
        return false
    }

    private fun String.isKaEnhet() = this.startsWith("42")
}
