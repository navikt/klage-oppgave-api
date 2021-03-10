package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Endringstype
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjon
import org.springframework.stereotype.Service

@Service
class OppgaveDiffService {
    fun diff(nyeste: OppgaveKopiVersjon, nestNyeste: OppgaveKopiVersjon): Pair<String, Endringstype>? {
        val endringsmap = mutableMapOf<String, Endringstype>()
        if (nyeste harEndretSaksbehandlerFra nestNyeste) {
            endringsmap["Saksbehandler på oppgave endret til ${nyeste.tilordnetRessurs}"] = Endringstype.VARSEL
        }
        if (nyeste harEndretEnhetTilAnnenKaEnhetFra nestNyeste) {
            endringsmap["Enhet på oppgave endret til ${nyeste.tildeltEnhetsnr}"] = Endringstype.VARSEL
        }
        if (nyeste harEndretEnhetTilIkkeKaEnhetFra nestNyeste) {
            endringsmap["Enhet på oppgave endret til ${nyeste.tildeltEnhetsnr}"] = Endringstype.FEIL
        }
        return when {
            endringsmap.isEmpty() -> null
            endringsmap.size == 1 -> endringsmap.firstRowToPair()
            else -> Pair(endringsmap.keys.joinToString(". "), highestSeverity(endringsmap.values))
        }
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

    private fun Map<String, Endringstype>.firstRowToPair() = this.entries.first().toPair()

    private fun String.isKaEnhet() = this.startsWith("42")

    private fun highestSeverity(endringstyper: Collection<Endringstype>) =
        when {
            endringstyper.contains(Endringstype.FEIL) -> Endringstype.FEIL
            else -> Endringstype.VARSEL
        }
}
