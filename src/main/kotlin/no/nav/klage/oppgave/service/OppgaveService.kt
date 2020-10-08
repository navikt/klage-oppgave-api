package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.AxsysClient
import no.nav.klage.oppgave.clients.MicrosoftGraphClient
import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.clients.PdlClient
import no.nav.klage.oppgave.domain.OppgaveResponse
import no.nav.klage.oppgave.domain.pdl.Navn
import no.nav.klage.oppgave.domain.view.OppgaveView
import no.nav.klage.oppgave.domain.view.OppgaveView.Bruker
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
        return oppgaver.map {
            OppgaveView(
                id = it.id,
                bruker = getBruker(it.aktoerId),
                type = it.beskrivelse ?: "",
                ytelse = it.tema,
                hjemmel = listOf("TODO hjemmel"),
                frist = it.fristFerdigstillelse,
                saksbehandler = "TODO saksbehandler"
            )
        }
    }

    private fun getBruker(aktoerId: String?): Bruker {
        return if (aktoerId == null) {
            Bruker("Mangler aktoerId", "Mangler aktoerId")
        } else {
            val person = pdlClient.getPersonInfo(aktoerId).data?.hentPerson
            return Bruker(
                fnr = person?.folkeregisteridentifikator?.firstOrNull()?.identifikasjonsnummer ?: "mangler",
                navn = person?.navn?.firstOrNull()?.toName() ?: "mangler"
            )
        }
    }

    private fun Navn.toName(): String {
        return "$fornavn $etternavn"
    }
}