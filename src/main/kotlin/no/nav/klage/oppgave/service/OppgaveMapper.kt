package no.nav.klage.oppgave.service


import no.nav.klage.oppgave.clients.PdlClient
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import no.nav.klage.oppgave.domain.gosys.*
import no.nav.klage.oppgave.domain.pdl.Navn
import no.nav.klage.oppgave.domain.view.*
import org.springframework.stereotype.Service
import no.nav.klage.oppgave.domain.gosys.Oppgave as OppgaveBackend
import no.nav.klage.oppgave.domain.view.Oppgave as OppgaveView

@Service
class OppgaveMapper(val pdlClient: PdlClient) {

    fun mapOppgaveToView(oppgaveBackend: OppgaveBackend, fetchPersoner: Boolean): OppgaveView {
        return mapOppgaverToView(listOf(oppgaveBackend), fetchPersoner).single()
    }

    fun mapEsOppgaverToView(esOppgaver: List<EsOppgave>, fetchPersoner: Boolean): List<OppgaveView> {
        val personer = mutableMapOf<String, OppgaveView.Person>()
        if (fetchPersoner) {
            personer.putAll(getPersoner(esOppgaver.mapNotNull { it.fnr }))
        }

        return esOppgaver.map { esOppgave ->
            OppgaveView(
                id = esOppgave.id.toString(),
                person = if (fetchPersoner) {
                    personer[esOppgave.fnr] ?: OppgaveView.Person("Mangler fnr", "Mangler navn")
                } else {
                    null
                },
                type = esOppgave.oppgavetype,
                ytelse = esOppgave.tema,
                hjemmel = esOppgave.hjemler?.first(),
                frist = esOppgave.fristFerdigstillelse,
                versjon = esOppgave.versjon.toInt()
            )
        }
    }

    private fun EsOppgave.toType(): String {
        return if (behandlingstema == null) {
            when (behandlingstype) {
                BEHANDLINGSTYPE_KLAGE -> TYPE_KLAGE
                BEHANDLINGSTYPE_ANKE -> TYPE_ANKE
                else -> "ukjent"
            }
        } else "mangler"
    }

    fun mapOppgaverToView(oppgaverBackend: List<OppgaveBackend>, fetchPersoner: Boolean): List<OppgaveView> {
        val personer = mutableMapOf<String, OppgaveView.Person>()
        if (fetchPersoner) {
            personer.putAll(getPersoner(getFnr(oppgaverBackend)))
        }

        return oppgaverBackend.map { oppgaveBackend ->
            OppgaveView(
                id = oppgaveBackend.id.toString(),
                person = if (fetchPersoner) {
                    personer[oppgaveBackend.getFnrForBruker()] ?: OppgaveView.Person("Mangler fnr", "Mangler navn")
                } else {
                    null
                },
                type = oppgaveBackend.toType(),
                ytelse = oppgaveBackend.toYtelse(),
                hjemmel = oppgaveBackend.metadata.toHjemmel(),
                frist = oppgaveBackend.fristFerdigstillelse,
                versjon = oppgaveBackend.versjon
            )
        }
    }

    private fun Map<String, String>?.toHjemmel() = this?.get(HJEMMEL) ?: "mangler"

    private fun OppgaveBackend.toType(): String {
        return if (behandlingstema == null) {
            when (behandlingstype) {
                BEHANDLINGSTYPE_KLAGE -> TYPE_KLAGE
                BEHANDLINGSTYPE_ANKE -> TYPE_ANKE
                else -> "ukjent"
            }
        } else "mangler"
    }

    private fun OppgaveBackend.toYtelse(): String = when (tema) {
        TEMA_SYK -> YTELSE_SYK
        TEMA_FOR -> YTELSE_FOR
        else -> tema
    }

    private fun getFnr(oppgaver: List<OppgaveBackend>) =
        oppgaver.mapNotNull {
            it.getFnrForBruker()
        }

    private fun getPersoner(fnrList: List<String>): Map<String, OppgaveView.Person> {
        val people = pdlClient.getPersonInfo(fnrList).data?.hentPersonBolk
        return people?.map {
            val fnr = it.folkeregisteridentifikator.first().identifikasjonsnummer
            fnr to OppgaveView.Person(
                fnr = fnr,
                navn = it.navn.firstOrNull()?.toName() ?: "mangler"
            )
        }?.toMap() ?: emptyMap()
    }

    private fun OppgaveBackend.getFnrForBruker() = identer?.find { i -> i.gruppe == Gruppe.FOLKEREGISTERIDENT }?.ident

    private fun Navn.toName() = "$fornavn $etternavn"
}