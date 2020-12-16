package no.nav.klage.oppgave.clients

import brave.Tracer
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.klage.oppgave.clients.gosys.Oppgave
import no.nav.klage.oppgave.clients.gosys.OppgaveClient
import no.nav.klage.oppgave.clients.gosys.OppgaveResponse
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.service.TokenService
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.LocalDate
import java.time.Month

@ExtendWith(MockKExtension::class)
internal class OppgaveClientTest {

    @MockK
    lateinit var tokenServiceMock: TokenService

    @MockK
    lateinit var tracerMock: Tracer

    @BeforeEach
    fun before() {
        every { tokenServiceMock.getStsSystembrukerToken() } returns "abc"
        every { tokenServiceMock.getSaksbehandlerAccessTokenWithOppgaveScope() } returns "abc"
        every { tracerMock.currentSpan().context().traceIdString() } returns "def"
    }

    @Test
    fun `frist is parsed correctly`() {
        val oppgaveResponse = getOppgaver(oppgaveResponseWithFrist())
        assertThat(oppgaveResponse.oppgaver.first().fristFerdigstillelse).isEqualTo(
            LocalDate.of(2020, Month.SEPTEMBER, 3)
        )
    }

    @Test
    fun `null frist is OK`() {
        val oppgaveResponse = getOppgaver(oppgaveResponseWithoutFrist())
        assertThat(oppgaveResponse.oppgaver.first().fristFerdigstillelse).isNull()
    }

    @Test
    fun `missing frist field is OK`() {
        val oppgaveResponse = getOppgaver(oppgaveResponseWithoutFristField())
        assertThat(oppgaveResponse.oppgaver.first().fristFerdigstillelse).isNull()
    }

    @Test
    fun `404 results in NotFound`() {
        Assertions.assertThrows(WebClientResponseException.NotFound::class.java) {
            getNonExistingOppgave()
        }
    }

    fun getOppgaver(jsonResponse: String): OppgaveResponse {
        val oppgaveClient = OppgaveClient(
            createShortCircuitWebClient(jsonResponse),
            tokenServiceMock,
            tracerMock,
            "appName",
        )

        return oppgaveClient.getOneSearchPage(OppgaverSearchCriteria(offset = 0, limit = 1))
    }

    fun getNonExistingOppgave(): Oppgave {
        val oppgaveClient = OppgaveClient(
            createShortCircuitWebClientWithStatus(oppgave404(), HttpStatus.NOT_FOUND),
            tokenServiceMock,
            tracerMock,
            "appName"
        )

        return oppgaveClient.getOppgave(3333)
    }

    @Language("json")
    fun oppgaveResponseWithFrist() = """
        {
          "antallTreffTotalt": 1,
          "oppgaver": [
            {
              "id": 315987998,
              "tildeltEnhetsnr": "4833",
              "opprettetAvEnhetsnr": "4833",
              "journalpostId": "467045409",
              "beskrivelse": "Klage/anke",
              "temagruppe": "FMLI",
              "tema": "FOR",
              "oppgavetype": "JFR",
              "versjon": 1,
              "opprettetAv": "srvfpfordel",
              "prioritet": "NORM",
              "status": "OPPRETTET",
              "metadata": {},
              "fristFerdigstillelse": "2020-09-03",
              "aktivDato": "2020-09-02",
              "opprettetTidspunkt": "2020-09-02T14:23:48.956+02:00"
            }
          ]
        }
    """

    @Language("json")
    fun oppgaveResponseWithoutFrist() = """
        {
          "antallTreffTotalt": 1,
          "oppgaver": [
            {
              "id": 315987998,
              "tildeltEnhetsnr": "4833",
              "opprettetAvEnhetsnr": "4833",
              "journalpostId": "467045409",
              "beskrivelse": "Klage/anke",
              "temagruppe": "FMLI",
              "tema": "FOR",
              "oppgavetype": "JFR",
              "versjon": 1,
              "opprettetAv": "srvfpfordel",
              "prioritet": "NORM",
              "status": "OPPRETTET",
              "metadata": {},
              "fristFerdigstillelse": null,
              "aktivDato": "2020-09-02",
              "opprettetTidspunkt": "2020-09-02T14:23:48.956+02:00"
            }
          ]
        }
    """

    @Language("json")
    fun oppgaveResponseWithoutFristField() = """
        {
          "antallTreffTotalt": 1,
          "oppgaver": [
            {
              "id": 315987998,
              "tildeltEnhetsnr": "4833",
              "opprettetAvEnhetsnr": "4833",
              "journalpostId": "467045409",
              "beskrivelse": "Klage/anke",
              "temagruppe": "FMLI",
              "tema": "FOR",
              "oppgavetype": "JFR",
              "versjon": 1,
              "opprettetAv": "srvfpfordel",
              "prioritet": "NORM",
              "status": "OPPRETTET",
              "metadata": {},
              "aktivDato": "2020-09-02",
              "opprettetTidspunkt": "2020-09-02T14:23:48.956+02:00"
            }
          ]
        }
    """

    @Language("json")
    fun oppgave404() = """
        {
          "uuid": "37389812-7af9-48ef-9733-e680d06e0978",
          "feilmelding": "Fant ingen oppgave med id: 3333"
        }
    """.trimIndent()


}