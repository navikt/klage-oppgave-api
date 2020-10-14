package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.AxsysClient
import no.nav.klage.oppgave.clients.MicrosoftGraphClient
import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.clients.PdlClient
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_KLAGE
import no.nav.klage.oppgave.domain.gosys.Oppgave
import no.nav.klage.oppgave.domain.gosys.Oppgave.Gruppe.FOLKEREGISTERIDENT
import no.nav.klage.oppgave.domain.gosys.OppgaveResponse
import no.nav.klage.oppgave.domain.pdl.Navn
import no.nav.klage.oppgave.domain.view.HJEMMEL
import no.nav.klage.oppgave.domain.view.OppgaveView
import no.nav.klage.oppgave.domain.view.OppgaveView.Bruker
import no.nav.klage.oppgave.domain.view.TYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.view.TYPE_KLAGE
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.stereotype.Service

@Service
class OppgaveService(
    val clientConfigurationProperties: ClientConfigurationProperties,
    val oAuth2AccessTokenService: OAuth2AccessTokenService,
    val axsysClient: AxsysClient,
    val microsoftGraphClient: MicrosoftGraphClient,
    val oppgaveClient: OppgaveClient,
    val pdlClient: PdlClient
) {

    fun getOppgaver(): List<OppgaveView> {
        return oppgaveClient.getOppgaver().toView()
    }

    fun getTilgangerForSaksbehandler() =
        axsysClient.getTilgangerForSaksbehandler(microsoftGraphClient.getNavIdent(getTokenWithGraphScope()))

    private fun getTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    private fun OppgaveResponse.toView(): List<OppgaveView> {

        val brukere = getBrukere(getFnr(this.oppgaver))

        return oppgaver.map {
            OppgaveView(
                id = it.id,
                bruker = brukere[getFnrFromOppgave(it)] ?: Bruker("Mangler fnr", "Mangler fnr"),
                type = it.toType(),
                ytelse = it.tema,
                hjemmel = it.metadata.toHjemmel(),
                frist = it.fristFerdigstillelse,
                saksbehandler = "todo"
            )
        }
    }

    private fun getFnr(oppgaver: List<Oppgave>): List<String> {
        return oppgaver.mapNotNull {
            getFnrFromOppgave(it)
        }
    }

    private fun getFnrFromOppgave(oppgave: Oppgave) =
        oppgave.identer?.find { i -> i.gruppe == FOLKEREGISTERIDENT }?.ident

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

    private fun Navn.toName(): String {
        return "$fornavn $etternavn"
    }

    private fun Map<String, String>?.toHjemmel(): List<String> {
        return listOf(this?.get(HJEMMEL) ?: "mangler")
    }

    private fun Oppgave.toType(): String {
        return if (behandlingstema == null) {
            when (behandlingstype) {
                BEHANDLINGSTYPE_KLAGE -> TYPE_KLAGE
                BEHANDLINGSTYPE_FEILUTBETALING -> TYPE_FEILUTBETALING
                else -> "mangler"
            }
        } else "mangler"
    }
}

