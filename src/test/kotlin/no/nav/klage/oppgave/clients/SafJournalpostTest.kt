package no.nav.klage.oppgave.clients

import brave.Tracer
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.klage.oppgave.clients.saf.graphql.Journalpost
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import no.nav.klage.oppgave.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SafJournalpostTest {

    @MockK
    lateinit var tokenUtilMock: TokenUtil

    @MockK
    lateinit var tracerMock: Tracer

    @BeforeEach
    fun before() {
        every { tokenUtilMock.getStsSystembrukerToken() } returns "abc"
        every { tokenUtilMock.getSaksbehandlerAccessTokenWithSafScope() } returns "abc"
        every { tracerMock.currentSpan().context().traceIdString() } returns "def"
    }

    @Test
    fun `journalpost mappes fint`() {
        val journalpostResponse = getJournalpost(journalpostResponse())
        assertThat(journalpostResponse).isNotNull
        assertThat(journalpostResponse!!.journalpostId).isEqualTo("123")
    }

    @Test
    fun `tom response fra saf er ogsaa gyldig`() {
        val dokumentoversiktBrukerResponse = getJournalpost(journalpostIkkeFunnetResponse())
        assertThat(dokumentoversiktBrukerResponse).isNull()
    }

    @Test
    fun `error response fra saf gir RuntimeException`() {
        assertThrows<RuntimeException> { getJournalpost(journalpostValidationErrorResponse()) }
    }

    fun getJournalpost(jsonResponse: String): Journalpost? {
        val safClient = SafGraphQlClient(
            createShortCircuitWebClient(jsonResponse),
            tokenUtilMock,
            tracerMock
        )

        return safClient.getJournalpostAsSaksbehandler("whatever")
    }

    @Language("json")
    fun journalpostResponse() = """
    {
      "data": {
        "journalpost": {
          "journalpostId": "123",
          "tittel": "MASKERT_FELT",
          "journalposttype": "N",
          "journalstatus": "FERDIGSTILT",
          "tema": "PEN",
          "temanavn": "Pensjon",
          "behandlingstema": null,
          "behandlingstemanavn": null,
          "sak": null,
          "bruker": {
            "id": "12345",
            "type": "AKTOERID"
          },
          "avsenderMottaker": {
            "id": null,
            "type": "NULL",
            "navn": "MASKERT_FELT",
            "land": null,
            "erLikBruker": false
          },
          "journalfoerendeEnhet": "4817",
          "journalfortAvNavn": "KONVERTERING",
          "opprettetAvNavn": null,
          "kanal": "INGEN_DISTRIBUSJON",
          "kanalnavn": "Ingen distribusjon",
          "skjerming": null,
          "datoOpprettet": "2008-12-06T00:00",
          "relevanteDatoer": [
            {
              "dato": "1996-07-01T00:00",
              "datotype": "DATO_DOKUMENT"
            },
            {
              "dato": "1996-07-01T00:00",
              "datotype": "DATO_JOURNALFOERT"
            }
          ],
          "antallRetur": null,
          "eksternReferanseId": null,
          "tilleggsopplysninger": [],
          "dokumenter": [
            {
              "dokumentInfoId": "123",
              "tittel": "MASKERT_FELT",
              "brevkode": null,
              "dokumentstatus": "FERDIGSTILT",
              "datoFerdigstilt": "1996-07-01T00:00",
              "originalJournalpostId": "123",
              "skjerming": null,
              "dokumentvarianter": [
                {
                  "variantformat": "ARKIV",
                  "filnavn": "10062939619-A1-019960700",
                  "saksbehandlerHarTilgang": true,
                  "skjerming": null
                }
              ]
            }
          ]
        }
      }
    }
    """

    @Language("json")
    fun journalpostIkkeFunnetResponse() = """
    {
      "errors": [
        {
          "message": "Journalpost med journalpostId=189898989891 ikke funnet.",
          "locations": [],
          "extensions": {
            "classification": "DataFetchingException"
          }
        }
      ],
      "data": {
        "journalpost": null
      }
    }
    """

    @Language("json")
    fun journalpostValidationErrorResponse() = """
    {
      "errors": [
        {
          "message": "Field 'journalpostId' of variable 'journalpostId' has coerced Null value for NonNull type 'String!'",
          "locations": [
            {
              "line": 1,
              "column": 7
            }
          ],
          "extensions": {
            "classification": "ValidationError"
          }
        }
      ]
    }
    """

}