package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.clients.PdlClient
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.gosys.*
import no.nav.klage.oppgave.domain.gosys.Gruppe.FOLKEREGISTERIDENT
import no.nav.klage.oppgave.domain.pdl.Navn
import no.nav.klage.oppgave.domain.view.*
import no.nav.klage.oppgave.exceptions.NotMatchingUserException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import no.nav.klage.oppgave.domain.gosys.Oppgave as OppgaveBackend
import no.nav.klage.oppgave.domain.view.Oppgave as OppgaveView


@Service
class OppgaveService(
    val oppgaveClient: OppgaveClient,
    val pdlClient: PdlClient,
    val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    val saksbehandlerRepository: SaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun searchOppgaver(navIdent: String, oppgaverSearchCriteria: OppgaverSearchCriteria): OppgaverRespons {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        if (innloggetIdent != navIdent) {
            throw NotMatchingUserException(
                "logged in user does not match sent in user. " +
                        "Logged in: $innloggetIdent, sent in: $navIdent"
            )
        }

        oppgaverSearchCriteria.enrichWithEnhetsnrForLoggedInUser(innloggetIdent)

        val oppgaveResponse = oppgaveClient.getOneSearchPage(oppgaverSearchCriteria)
        return OppgaverRespons(
            antallTreffTotalt = oppgaveResponse.antallTreffTotalt,
            oppgaver = oppgaveResponse.toOppgaverView(oppgaverSearchCriteria.projection)
        )
    }

    private fun OppgaverSearchCriteria.enrichWithEnhetsnrForLoggedInUser(innloggetIdent: String) {
        val tilgangerForSaksbehandler = saksbehandlerRepository.getTilgangerForSaksbehandler(innloggetIdent)
        if (tilgangerForSaksbehandler.enheter.size > 1) {
            logger.warn("Saksbehandler ({}) had more than one enhet. Only using the first.", innloggetIdent)
        }
        this.enhetsnr = tilgangerForSaksbehandler.enheter.first().enhetId
    }

    private fun OppgaveResponse.toOppgaverView(projection: OppgaverSearchCriteria.Projection?): List<OppgaveView> {
        val fetchPersoner = projection == OppgaverSearchCriteria.Projection.UTVIDET
        val personer = mutableMapOf<String, OppgaveView.Person>()
        if (fetchPersoner) {
            personer.putAll(getPersoner(getFnr(this.oppgaver)))
        }

        return oppgaver.map { oppgaveBackend ->
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

    private fun Navn.toName() = "$fornavn $etternavn"

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

    private fun OppgaveBackend.getFnrForBruker() = identer?.find { i -> i.gruppe == FOLKEREGISTERIDENT }?.ident

    fun assignOppgave(oppgaveId: Long, saksbehandlerIdent: String?, oppgaveVersjon: Int?) {
        val endreOppgave = oppgaveClient.getOppgave(oppgaveId).toEndreOppgave()
        logger.info(
            "Endrer tilordnetRessurs for oppgave {} fra {} til {}, versjon er {}",
            endreOppgave.id,
            endreOppgave.tilordnetRessurs,
            saksbehandlerIdent,
            oppgaveVersjon
        )
        endreOppgave.apply {
            tilordnetRessurs = saksbehandlerIdent;
            versjon = oppgaveVersjon
        }

        updateOppgave(oppgaveId, endreOppgave)
    }

    private fun updateOppgave(
        oppgaveId: Long,
        oppgave: EndreOppgave
    ) {
        oppgaveClient.putOppgave(oppgaveId, oppgave)
    }

//    fun getOppgave(oppgaveId: Long): OppgaveView {
//        val oppgaveBackend = oppgaveClient.getOppgave(oppgaveId)
//        return OppgaveView(
//            id = oppgaveBackend.id.toString(),
//            type = oppgaveBackend.toType(),
//            ytelse = oppgaveBackend.toYtelse(),
//            hjemmel = oppgaveBackend.metadata.toHjemmel(),
//            frist = oppgaveBackend.fristFerdigstillelse,
//            versjon = oppgaveBackend.versjon
//        )
//    }


//    fun setHjemmel(oppgaveId: Long, hjemmel: String, oppgaveVersjon: Int?): OppgaveView {
//        val oppgave = oppgaveRepository.getOppgave(oppgaveId).toEndreOppgave()
//        oppgave.apply {
//            setHjemmel(hjemmel)
//            versjon = oppgaveVersjon
//        }
//
//        return updateAndReturn(oppgaveId, oppgave)
//    }
//
//    private fun EndreOppgave.setHjemmel(hjemmel: String) {
//        if (metadata == null) {
//            metadata = mutableMapOf()
//        }
//        logger.info("Endrer hjemmel for oppgave {} fra {} til {}", id, metadata?.get(HJEMMEL), hjemmel)
//        metadata!![HJEMMEL] = hjemmel
//    }
//

    fun ubehandledeKlager(): Int {
        val searchCriteria = OppgaverSearchCriteria(
            typer = listOf("Klage"),
            ytelser = listOf("Sykepenger"),
            statuskategori = OppgaverSearchCriteria.Statuskategori.AAPEN,
            offset = 0,
            limit = 1
        )
        return oppgaveClient.getOppgaveCount(searchCriteria)
    }

    fun klagerOverFrist(): Int {
        val searchCriteria = OppgaverSearchCriteria(
            typer = listOf("Klage"),
            ytelser = listOf("Sykepenger"),
            statuskategori = OppgaverSearchCriteria.Statuskategori.AAPEN,
            fristFom = LocalDate.now().plusDays(1),
            offset = 0,
            limit = 1
        )
        return oppgaveClient.getOppgaveCount(searchCriteria)
    }

    fun klagerInnsendtIPeriode(numberOfDays: Int): Int {
        val startOfPeriod = startOfPeriod(numberOfDays)
        val endOfPeriod = endOfPeriod(numberOfDays)
        
        val searchCriteria1 = OppgaverSearchCriteria(
            typer = listOf("Klage"),
            ytelser = listOf("Sykepenger"),
            statuskategori = OppgaverSearchCriteria.Statuskategori.AAPEN,
            opprettetFom = startOfPeriod,
            opprettetTom = endOfPeriod,
            offset = 0,
            limit = 1
        )
        val searchCriteria2 = OppgaverSearchCriteria(
            typer = listOf("Klage"),
            ytelser = listOf("Sykepenger"),
            statuskategori = OppgaverSearchCriteria.Statuskategori.AVSLUTTET,
            opprettetFom = startOfPeriod,
            opprettetTom = endOfPeriod,
            offset = 0,
            limit = 1
        )
        return oppgaveClient.getOppgaveCount(searchCriteria1) + oppgaveClient.getOppgaveCount(searchCriteria2)
    }

    fun klagerFerdigbehandletIPeriode(numberOfDays: Int): Int {
        val startOfPeriod = startOfPeriod(numberOfDays)
        val endOfPeriod = endOfPeriod(numberOfDays)

        val searchCriteria = OppgaverSearchCriteria(
            typer = listOf("Klage"),
            ytelser = listOf("Sykepenger"),
            statuskategori = OppgaverSearchCriteria.Statuskategori.AVSLUTTET,
            ferdigstiltFom = startOfPeriod,
            ferdigstiltTom = endOfPeriod,
            offset = 0,
            limit = 1
        )
        return oppgaveClient.getOppgaveCount(searchCriteria)
    }

    private fun startOfPeriod(numberOfDays: Int): LocalDateTime =
        LocalDate.now().minusDays(numberOfDays.toLong()).atTime(LocalTime.MIN)

    private fun endOfPeriod(numberOfDays: Int): LocalDateTime =
        LocalDate.now().minusDays(todayOrYesterday(numberOfDays)).atTime(LocalTime.MAX)

    private fun todayOrYesterday(numberOfDays: Int): Long = if (numberOfDays == 0) {
        0
    } else {
        1
    }

}