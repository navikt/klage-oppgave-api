package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.AxsysClient
import no.nav.klage.oppgave.clients.PdlClient
import no.nav.klage.oppgave.domain.gosys.*
import no.nav.klage.oppgave.domain.gosys.Gruppe.FOLKEREGISTERIDENT
import no.nav.klage.oppgave.domain.pdl.Navn
import no.nav.klage.oppgave.domain.view.HJEMMEL
import no.nav.klage.oppgave.domain.view.OppgaveView
import no.nav.klage.oppgave.domain.view.OppgaveView.Bruker
import no.nav.klage.oppgave.domain.view.OppgaveView.Saksbehandler
import no.nav.klage.oppgave.domain.view.TYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.view.TYPE_KLAGE
import no.nav.klage.oppgave.repositories.OppgaveRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.stereotype.Service


@Service
class OppgaveService(
    val clientConfigurationProperties: ClientConfigurationProperties,
    val oAuth2AccessTokenService: OAuth2AccessTokenService,
    val axsysClient: AxsysClient,
    val oppgaveRepository: OppgaveRepository,
    val pdlClient: PdlClient,
    val saksbehandlerRepository: SaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getOppgaver(): List<OppgaveView> {
        return oppgaveRepository.getOppgaver().toView().also { logger.info("Returnerer {} oppgaver", it.size) }
    }

    fun searchOppgaver(oppgaveSearchCriteria: OppgaveSearchCriteria): List<OppgaveView> {
        return oppgaveRepository.searchOppgaver(oppgaveSearchCriteria).toView()
            .filter { it.hjemmel == oppgaveSearchCriteria.hjemmel }
    }

    fun getTilgangerForSaksbehandler() =
        axsysClient.getTilgangerForSaksbehandler(saksbehandlerRepository.getNavIdent(getTokenWithGraphScope()))

    private fun getTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    private fun getAppTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["app"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    private fun OppgaveResponse.toView(): List<OppgaveView> {

        val brukere = getBrukere(getFnr(this.oppgaver))
        val saksbehandlere = getSaksbehandlere(getSaksbehandlerIdenter(oppgaver))

        return oppgaver.map {
            toView(it, brukere, saksbehandlere)
        }
    }

    private fun toView(
        it: Oppgave,
        brukere: Map<String, Bruker>,
        saksbehandlere: Map<String, Saksbehandler>
    ): OppgaveView {
        return OppgaveView(
            id = it.id,
            bruker = brukere[it.getFnrForBruker()] ?: Bruker("Mangler fnr", "Mangler fnr"),
            type = it.toType(),
            ytelse = it.tema,
            hjemmel = it.metadata.toHjemmel(),
            frist = it.fristFerdigstillelse,
            saksbehandler = saksbehandlere[it.tilordnetRessurs]
        )
    }

    private fun getSaksbehandlere(identer: Set<String>): Map<String, Saksbehandler> {
        logger.debug("Getting names for saksbehandlere")
        val namesForSaksbehandlere = saksbehandlerRepository.getNamesForSaksbehandlere(identer, getAppTokenWithGraphScope())
        return namesForSaksbehandlere.map {
            it.key to Saksbehandler(
                ident = it.key,
                navn = it.value
            )
        }.toMap()
    }

    private fun getFnr(oppgaver: List<Oppgave>) =
        oppgaver.mapNotNull {
            it.getFnrForBruker()
        }

    private fun getSaksbehandlerIdenter(oppgaver: List<Oppgave>) =
        oppgaver.mapNotNull {
            it.tilordnetRessurs
        }.toSet()

    private fun getSaksbehandlerIdenterForEndring(oppgaver: List<EndreOppgave>) =
        oppgaver.mapNotNull {
            it.tilordnetRessurs
        }.toSet()

    private fun getBrukere(fnrList: List<String>): Map<String, Bruker> {
        val people = pdlClient.getPersonInfo(fnrList).data?.hentPersonBolk
        return people?.map {
            val fnr = it.folkeregisteridentifikator.first().identifikasjonsnummer
            fnr to Bruker(
                fnr = fnr,
                navn = it.navn.firstOrNull()?.toName() ?: "mangler"
            )
        }?.toMap() ?: emptyMap()
    }

    private fun Navn.toName() = "$fornavn $etternavn"

    private fun Map<String, String>?.toHjemmel() = this?.get(HJEMMEL) ?: "mangler"

    private fun Oppgave.toType(): String {
        return if (behandlingstema == null) {
            when (behandlingstype) {
                BEHANDLINGSTYPE_KLAGE -> TYPE_KLAGE
                BEHANDLINGSTYPE_FEILUTBETALING -> TYPE_FEILUTBETALING
                else -> "mangler"
            }
        } else "mangler"
    }

    private fun Oppgave.getFnrForBruker() = identer?.find { i -> i.gruppe == FOLKEREGISTERIDENT }?.ident

    fun assignRandomHjemler(): List<OppgaveView> {
        val oppgaver = oppgaveRepository.getOppgaver().oppgaver
            .map { it.toEndreOppgave() }
            .map {
                it.setHjemmel(hjemler.random())
                oppgaveRepository.updateOppgave(it.id, it)
            }
        val brukere = getBrukere(getFnr(oppgaver))
        val saksbehandlere = getSaksbehandlere(getSaksbehandlerIdenter(oppgaver))

        return oppgaver.map {
            toView(it, brukere, saksbehandlere)
        }
    }

    fun setHjemmel(oppgaveId: Int, hjemmel: String): OppgaveView {
        var oppgave = oppgaveRepository.getOppgave(oppgaveId).toEndreOppgave()
        oppgave.setHjemmel(hjemmel)
        return updateAndReturn(oppgaveId, oppgave)
    }

    private fun EndreOppgave.setHjemmel(hjemmel: String) {
        if (metadata == null) {
            metadata = mutableMapOf()
        }
        logger.info("Endrer hjemmel for oppgave {} fra {} til {}", id, metadata?.get(HJEMMEL), hjemmel)
        metadata!![HJEMMEL] = hjemmel
    }

    fun assignOppgave(oppgaveId: Int, saksbehandlerIdent: String?): OppgaveView {
        val oppgave = oppgaveRepository.getOppgave(oppgaveId).toEndreOppgave()
        logger.info("Endrer tilordnetRessurs for oppgave {} fra {} til {}", oppgave.id, oppgave.tilordnetRessurs, saksbehandlerIdent)
        oppgave.tilordnetRessurs = saksbehandlerIdent

        return updateAndReturn(oppgaveId, oppgave)
    }

    fun getOppgave(oppgaveId: Int): OppgaveView {
        val oppgave = oppgaveRepository.getOppgave(oppgaveId)
        val brukere = getBrukere(getFnr(listOf(oppgave)))
        val saksbehandlere = getSaksbehandlere(getSaksbehandlerIdenter(listOf(oppgave)))
        return toView(oppgave, brukere, saksbehandlere)
    }

    private fun updateAndReturn(
        oppgaveId: Int,
        oppgave: EndreOppgave
    ): OppgaveView {
        val endretOppgave = oppgaveRepository.updateOppgave(oppgaveId, oppgave)
        val brukere = getBrukere(getFnr(listOf(endretOppgave)))
        val saksbehandlere = getSaksbehandlere(getSaksbehandlerIdenterForEndring(listOf(oppgave)))
        return toView(endretOppgave, brukere, saksbehandlere)
    }
}

data class OppgaveSearchCriteria(
    val type: String? = null,
    val ytelse: String? = null,
    val hjemmel: String? = null,
    val erTildeltSaksbehandler: Boolean? = null,
    val saksbehandler: String? = null
)
