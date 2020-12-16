package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.gosys.OppgaveClient
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class OppgaveStatistikkService(
    val oppgaveClient: OppgaveClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

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