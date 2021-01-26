package no.nav.klage.oppgave.clients

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.klage.oppgave.clients.pdl.graphql.HentPersonResponse
import no.nav.klage.oppgave.clients.pdl.graphql.PdlClient
import no.nav.klage.oppgave.service.TokenService
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class PdlClientTest {

    @MockK
    lateinit var tokenServiceMock: TokenService

    @BeforeEach
    fun before() {
        every { tokenServiceMock.getStsSystembrukerToken() } returns "abc"
    }

    @Test
    fun `pdl response kan mappes selv med tomme arrays`() {
        val hentPersonResponse = getHentPersonResponse(pdlResponse())
        assertThat(hentPersonResponse.data).isNotNull
        assertThat(hentPersonResponse.data!!.hentPerson!!.navn.first().fornavn).isEqualTo("AREMARK")
    }

    fun getHentPersonResponse(jsonResponse: String): HentPersonResponse {
        val pdlClient = PdlClient(
            createShortCircuitWebClient(jsonResponse),
            tokenServiceMock
        )

        return pdlClient.getPersonInfo("fnr")
    }

    @Language("json")
    fun pdlResponse() = """
        {
          "data": {
            "hentPerson": {
              "navn": [
                {
                  "fornavn": "AREMARK",
                  "mellomnavn": null,
                  "etternavn": "TESTFAMILIEN"
                }
              ],
              "kjoenn": [
                {
                  "kjoenn": "MANN"
                }
              ],
              "adressebeskyttelse": []
            }
          }
        }
    """


}