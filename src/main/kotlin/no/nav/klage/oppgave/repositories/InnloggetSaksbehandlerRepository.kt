package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.service.TokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class InnloggetSaksbehandlerRepository(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val tokenService: TokenService,
    @Value("\${ROLE_GOSYS_OPPGAVE_BEHANDLER}") private val gosysSaksbehandlerRole: String,
    @Value("\${ROLE_KLAGE_SAKSBEHANDLER}") private val saksbehandlerRole: String,
    @Value("\${ROLE_KLAGE_FAGANSVARLIG}") private val fagansvarligRole: String,
    @Value("\${ROLE_KLAGE_LEDER}") private val lederRole: String,
    @Value("\${ROLE_KLAGE_MERKANTIL}") private val merkantilRole: String,
    @Value("\${ROLE_KLAGE_FORTROLIG}") private val kanBehandleFortroligRole: String,
    @Value("\${ROLE_KLAGE_STRENGT_FORTROLIG}") private val kanBehandleStrengtFortroligRole: String,
    @Value("\${ROLE_KLAGE_EGEN_ANSATT}") private val kanBehandleEgenAnsattRole: String,
    @Value("\${ROLE_ADMIN}") private val adminRole: String
) {

    fun getTilgangerForSaksbehandler(): EnheterMedLovligeTemaer =
        saksbehandlerRepository.getTilgangerForSaksbehandler(getInnloggetIdent())

    fun getInnloggetIdent() = tokenService.getIdent()

    fun erAdmin(): Boolean = tokenService.getRollerFromToken().hasRole(adminRole)

    fun erLeder(): Boolean = tokenService.getRollerFromToken().hasRole(lederRole)

    fun erFagansvarlig(): Boolean = tokenService.getRollerFromToken().hasRole(fagansvarligRole)

    fun erSaksbehandler(): Boolean =
        tokenService.getRollerFromToken().hasRole(saksbehandlerRole) || tokenService.getRollerFromToken()
            .hasRole(gosysSaksbehandlerRole)

    fun kanBehandleFortrolig(): Boolean = tokenService.getRollerFromToken().hasRole(kanBehandleFortroligRole)

    fun kanBehandleStrengtFortrolig(): Boolean =
        tokenService.getRollerFromToken().hasRole(kanBehandleStrengtFortroligRole)

    fun kanBehandleEgenAnsatt(): Boolean = tokenService.getRollerFromToken().hasRole(kanBehandleEgenAnsattRole)

    fun harTilgangTilEnhet(enhetId: String): Boolean {
        return getTilgangerForSaksbehandler().enheter.firstOrNull { it.enhetId == enhetId } != null
    }

    private fun List<String>.hasRole(role: String) = any { it.contains(role) }
}
