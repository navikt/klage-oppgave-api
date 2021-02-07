package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.api.OppgaveFacade
import no.nav.klage.oppgave.api.mapper.OppgaveMapper
import no.nav.klage.oppgave.api.view.HJEMMEL
import no.nav.klage.oppgave.clients.gosys.*
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime

internal class OppgaveFacadeTest {

    @Test
    fun `type is klage`() {
        val oppgaveFacade = oppgaveFacadeWithType(BEHANDLINGSTYPE_KLAGE)
        assertThat(oppgaveFacade.searchOppgaver(mockk(relaxed = true)).oppgaver.first().type).isEqualTo(TYPE_KLAGE)
    }

    @Test
    fun `type is feilutbetaling`() {
        val oppgaveService = oppgaveFacadeWithType(BEHANDLINGSTYPE_FEILUTBETALING)
        assertThat(oppgaveService.searchOppgaver(mockk(relaxed = true)).oppgaver.first().type).isEqualTo(
            TYPE_FEILUTBETALING
        )
    }

    @Test
    fun `unknown type`() {
        val oppgaveService = oppgaveFacadeWithType("somethingelse")
        assertThat(oppgaveService.searchOppgaver(mockk(relaxed = true)).oppgaver.first().type).isEqualTo("ukjent")
    }

    @Test
    fun `hjemmel is set correctly`() {
        val hjemmel = "8-1"
        val oppgaveService = oppgaveServiceWithHjemmel(hjemmel)
        assertThat(oppgaveService.searchOppgaver(mockk(relaxed = true)).oppgaver.first().hjemmel).isEqualTo(hjemmel)
    }

    @Test
    fun `missing hjemmel does not fail`() {
        val oppgaveService = oppgaveFacadeWithType("something")

        assertThat(oppgaveService.searchOppgaver(mockk(relaxed = true)).oppgaver.first().hjemmel).isEqualTo("mangler")
    }

    @Test
    fun `fnr is mapped correctly`() {
        val fnr = "12345678910"
        val oppgaveClientMock = mockk<OppgaveClient>()
        every { oppgaveClientMock.getOneSearchPage(any()) } returns getOppgaveResponseWithIdenter(fnr)

        val pdlFacadeMock = mockk<PdlFacade>()
        every { pdlFacadeMock.getPersonerInfo(listOf(fnr)) } returns getHentPersonResponse()

        val oppgaveFacade = OppgaveFacade(
            OppgaveService(
                oppgaveClientMock
            ),
            OppgaveMapper(pdlFacadeMock, mockk(relaxed = true)),
            mockk(),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true)
        )

        val oppgaverSearchCriteriaMock = mockk<OppgaverSearchCriteria>(relaxed = true)
        every { oppgaverSearchCriteriaMock.isProjectionUtvidet() } returns true

        assertThat(
            oppgaveFacade.searchOppgaver(oppgaverSearchCriteriaMock).oppgaver.first().person?.fnr
        ).isEqualTo(fnr)
    }

    private fun getHentPersonResponse(): List<Person> {
        return listOf(
            Person(
                foedselsnr = "12345678910",
                fornavn = "Test",
                mellomnavn = null,
                etternavn = "Person",
                navn = "Test Person",
                beskyttelsesbehov = null
            )
        )
    }

    private fun oppgaveServiceWithHjemmel(hjemmel: String): OppgaveFacade {
        val oppgaveClientMock = mockk<OppgaveClient>()
        every { oppgaveClientMock.getOneSearchPage(any()) } returns getOppgaveResponseWithHjemmel(hjemmel)

        val pdlFacadeMock = mockk<PdlFacade>()
        every { pdlFacadeMock.getPersonerInfo(any()) } returns getHentPersonResponse()

        val oppgaveFacade = OppgaveFacade(
            OppgaveService(
                oppgaveClientMock
            ),
            OppgaveMapper(pdlFacadeMock, mockk(relaxed = true)),
            mockk(),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true)
        )
        return oppgaveFacade
    }

    private fun oppgaveFacadeWithType(type: String): OppgaveFacade {
        val oppgaveClientMock = mockk<OppgaveClient>()
        every { oppgaveClientMock.getOneSearchPage(any()) } returns getOppgaveResponseWithType(type)

        val pdlFacadeMock = mockk<PdlFacade>()
        every { pdlFacadeMock.getPersonerInfo(any()) } returns getHentPersonResponse()

        return OppgaveFacade(
            OppgaveService(
                oppgaveClientMock
            ),
            OppgaveMapper(pdlFacadeMock, mockk(relaxed = true)),
            mockk(),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
        )
    }

    private fun getOppgaveResponseWithType(type: String) = OppgaveResponse(
        antallTreffTotalt = 1,
        oppgaver = listOf(
            Oppgave(
                id = 1,
                behandlingstype = type,
                fristFerdigstillelse = LocalDate.now(),
                tema = "SYK",
                versjon = 0,
                aktivDato = LocalDate.now(),
                oppgavetype = "ae0058",
                opprettetAv = "H149390",
                opprettetTidspunkt = OffsetDateTime.now(),
                prioritet = Prioritet.NORM,
                status = Status.OPPRETTET,
                tildeltEnhetsnr = "4209"
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
                metadata = mapOf(HJEMMEL to hjemmel),
                versjon = 0,
                aktivDato = LocalDate.now(),
                oppgavetype = "ae0058",
                opprettetAv = "H149390",
                opprettetTidspunkt = OffsetDateTime.now(),
                prioritet = Prioritet.NORM,
                status = Status.OPPRETTET,
                tildeltEnhetsnr = "4209"
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
                    Ident(
                        ident = "321321",
                        gruppe = Gruppe.AKTOERID
                    ),
                    Ident(
                        ident = fnr,
                        gruppe = Gruppe.FOLKEREGISTERIDENT
                    )
                ),
                versjon = 0,
                aktivDato = LocalDate.now(),
                oppgavetype = "ae0058",
                opprettetAv = "H149390",
                opprettetTidspunkt = OffsetDateTime.now(),
                prioritet = Prioritet.NORM,
                status = Status.OPPRETTET,
                tildeltEnhetsnr = "4209"
            )
        )
    )

}