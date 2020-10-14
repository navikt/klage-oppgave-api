package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.clients.PdlClient
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_KLAGE
import no.nav.klage.oppgave.domain.gosys.Oppgave
import no.nav.klage.oppgave.domain.gosys.OppgaveResponse
import no.nav.klage.oppgave.domain.pdl.*
import no.nav.klage.oppgave.domain.view.HJEMMEL
import no.nav.klage.oppgave.domain.view.TYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.view.TYPE_KLAGE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OppgaveServiceTest {

    @Test
    fun `type is klage`() {
        val oppgaveService = oppgaveServiceWithType(BEHANDLINGSTYPE_KLAGE)
        assertThat(oppgaveService.getOppgaver().first().type).isEqualTo(TYPE_KLAGE)
    }

    @Test
    fun `type is feilutbetaling`() {
        val oppgaveService = oppgaveServiceWithType(BEHANDLINGSTYPE_FEILUTBETALING)
        assertThat(oppgaveService.getOppgaver().first().type).isEqualTo(TYPE_FEILUTBETALING)
    }

    @Test
    fun `unknown type`() {
        val oppgaveService = oppgaveServiceWithType("somethingelse")
        assertThat(oppgaveService.getOppgaver().first().type).isEqualTo("mangler")
    }

    @Test
    fun `hjemmel is set correctly`() {
        val hjemmel = "8-1"
        val oppgaveService = oppgaveServiceWithHjemmel(hjemmel)
        assertThat(oppgaveService.getOppgaver().first().hjemmel.first()).isEqualTo(hjemmel)
    }

    @Test
    fun `missing hjemmel does not fail`() {
        val oppgaveService = oppgaveServiceWithType("something")
        assertThat(oppgaveService.getOppgaver().first().hjemmel.first()).isEqualTo("mangler")
    }

    @Test
    fun `fnr is mapped correctly`() {
        val fnr = "12345678910"
        val oppgaveClient = mockk<OppgaveClient>()
        every { oppgaveClient.getOppgaver() } returns getOppgaveResponseWithIdenter(fnr)

        val pdlClientMock = mockk<PdlClient>()
        every { pdlClientMock.getPersonInfo(listOf(fnr)) } returns getHentPersonResponse()

        val oppgaveService = OppgaveService(
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            oppgaveClient,
            pdlClientMock
        )

        assertThat(oppgaveService.getOppgaver().first().bruker.fnr).isEqualTo(fnr)
    }

    private fun getHentPersonResponse(): HentPersonResponse {
        return HentPersonResponse(
            data = HentPersonBolk(
                listOf(
                    Person(
                        navn = listOf(
                            Navn(
                                fornavn = "Test",
                                etternavn = "Person"
                            )
                        ),
                        folkeregisteridentifikator = listOf(
                            Folkeregisteridentifikator(
                                identifikasjonsnummer = "12345678910",
                                type = "FNR",
                                status = ""
                            )
                        )
                    )
                )
            )
        )
    }

    private fun oppgaveServiceWithHjemmel(hjemmel: String): OppgaveService {
        val oppgaveClientMock = mockk<OppgaveClient>()
        every { oppgaveClientMock.getOppgaver() } returns getOppgaveResponseWithHjemmel(hjemmel)

        val pdlClientMock = mockk<PdlClient>()
        every { pdlClientMock.getPersonInfo(any()) } returns getHentPersonResponse()

        val oppgaveService = OppgaveService(
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            oppgaveClientMock,
            pdlClientMock
        )
        return oppgaveService
    }

    private fun oppgaveServiceWithType(type: String): OppgaveService {
        val oppgaveClientMock = mockk<OppgaveClient>()
        every { oppgaveClientMock.getOppgaver() } returns getOppgaveResponseWithType(type)

        val pdlClientMock = mockk<PdlClient>()
        every { pdlClientMock.getPersonInfo(any()) } returns getHentPersonResponse()

        return OppgaveService(
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            oppgaveClientMock,
            pdlClientMock
        )
    }

    private fun getOppgaveResponseWithType(type: String) = OppgaveResponse(
        antallTreffTotalt = 1,
        oppgaver = listOf(
            Oppgave(
                id = 1,
                behandlingstype = type,
                fristFerdigstillelse = LocalDate.now(),
                tema = "SYK"
            )
        )
    )

    private fun getOppgaveResponseWithHjemmel(hjemmel: String) = OppgaveResponse(
        antallTreffTotalt = 1,
        oppgaver = listOf(
            Oppgave(
                id = 1,
                fristFerdigstillelse = LocalDate.now(),
                tema = "SYK",
                metadata = mapOf(HJEMMEL to hjemmel)
            )
        )
    )

    private fun getOppgaveResponseWithIdenter(fnr: String) = OppgaveResponse(
        antallTreffTotalt = 1,
        oppgaver = listOf(
            Oppgave(
                id = 1,
                fristFerdigstillelse = LocalDate.now(),
                tema = "SYK",
                identer = listOf(
                    Oppgave.Ident(
                        ident = "321321",
                        gruppe = Oppgave.Gruppe.AKTOERID
                    ),
                    Oppgave.Ident(
                        ident = fnr,
                        gruppe = Oppgave.Gruppe.FOLKEREGISTERIDENT
                    )
                )
            )
        )
    )

}