package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.service.TokenService
import org.springframework.stereotype.Service

@Service
class InnloggetSaksbehandlerRepository(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val tokenService: TokenService
) {

    fun getTilgangerForSaksbehandler() =
        saksbehandlerRepository.getTilgangerForSaksbehandler(getInnloggetIdent())

    fun getInnloggetIdent() = tokenService.getIdent()

    fun erLeder(): Boolean = saksbehandlerRepository.erLeder(getInnloggetIdent())

    fun erFagansvarlig(): Boolean = saksbehandlerRepository.erFagansvarlig(getInnloggetIdent())

    fun erSaksbehandler(): Boolean = saksbehandlerRepository.erSaksbehandler(getInnloggetIdent())

    fun getRoller(): List<String> = saksbehandlerRepository.getRoller(getInnloggetIdent())

}
