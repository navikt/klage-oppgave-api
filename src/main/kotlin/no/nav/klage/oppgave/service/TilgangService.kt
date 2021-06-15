package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.exceptions.KlagebehandlingAvsluttetException
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val pdlFacade: PdlFacade,
    private val egenAnsattService: EgenAnsattService,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun verifySaksbehandlersSkrivetilgang(klagebehandling: Klagebehandling) {
        if (klagebehandling.avsluttetAvSaksbehandler != null || klagebehandling.avsluttet != null) {
            throw KlagebehandlingAvsluttetException("Kan ikke endre avsluttet klagebehandling")
        }
        val ident = innloggetSaksbehandlerRepository.getInnloggetIdent()
        if (!saksbehandlerHarSkrivetilgang(klagebehandling, ident)) {
            throw MissingTilgangException("Kun tildelt saksbehandler/medunderskriver kan endre klagebehandlingen")
        }
    }

    private fun saksbehandlerHarSkrivetilgang(klagebehandling: Klagebehandling, ident: String): Boolean =
        ident == klagebehandling.tildeling?.saksbehandlerident || ident == klagebehandling.medunderskriver?.saksbehandlerident

    fun verifySystembrukersSkrivetilgang(klagebehandling: Klagebehandling) {
        if (klagebehandling.avsluttet != null) {
            throw KlagebehandlingAvsluttetException("Kan ikke endre avsluttet klagebehandling")
        }
    }

    fun verifySaksbehandlersTilgangTil(fnr: String) {
        if (!harSaksbehandlerTilgangTil(fnr)) {
            throw MissingTilgangException("Not authorized to access this user")
        }
    }

    fun verifySaksbehandlersTilgangTilEnhet(enhetId: String) {
        if (!innloggetSaksbehandlerRepository.harTilgangTilEnhet(enhetId)) {
            throw MissingTilgangException("Not authorized to act as this enhet")
        }
    }

    fun harSaksbehandlerTilgangTil(fnr: String): Boolean {
        val personInfo = pdlFacade.getPersonInfo(fnr)
        if (personInfo.harBeskyttelsesbehovFortrolig()) {
            securelogger.info("erFortrolig")
            if (innloggetSaksbehandlerRepository.kanBehandleFortrolig()) {
                securelogger.info("Access granted to fortrolig for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
            } else {
                securelogger.info("Access denied to fortrolig for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
                return false
            }
        }
        if (personInfo.harBeskyttelsesbehovStrengtFortrolig()) {
            securelogger.info("erStrengtFortrolig")
            if (innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig()) {
                securelogger.info("Access granted to strengt fortrolig for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
            } else {
                securelogger.info("Access denied to strengt fortrolig for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
                return false
            }
        }
        if (egenAnsattService.erEgenAnsatt(fnr)) {
            securelogger.info("erEgenAnsatt")
            if (innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt()) {
                securelogger.info("Access granted to egen ansatt for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
            } else {
                securelogger.info("Access denied to egen ansatt for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
                return false
            }
        }
        return true
    }
}
