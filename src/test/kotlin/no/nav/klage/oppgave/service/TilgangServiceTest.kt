package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.Beskyttelsesbehov
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.*
import no.nav.klage.oppgave.exceptions.KlagebehandlingAvsluttetException
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class TilgangServiceTest {

    private val pdlFacade: PdlFacade = mockk()

    private val egenAnsattService: EgenAnsattService = mockk()

    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository = mockk()

    private val tilgangService = TilgangService(pdlFacade, egenAnsattService, innloggetSaksbehandlerRepository)

    @Test
    fun `verifySaksbehandlersSkrivetilgang gir feil ved avsluttet`() {
        val klage = Klagebehandling(
            versjon = 2L,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                skalMottaKopi = false
            ),
            tema = Tema.OMS,
            type = Type.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel.FTL_8_7
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            kildesystem = Fagsystem.FS39,
            mottakId = UUID.randomUUID(),
            avsluttet = LocalDateTime.now()
        )

        assertThrows<KlagebehandlingAvsluttetException> { tilgangService.verifySaksbehandlersSkrivetilgang(klage) }
    }

    @Test
    fun `verifySaksbehandlersSkrivetilgang gir feil ved avsluttet av saksbehandler`() {
        val klage = Klagebehandling(
            versjon = 2L,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                skalMottaKopi = false
            ),
            tema = Tema.OMS,
            type = Type.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel.FTL_8_7
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            kildesystem = Fagsystem.FS39,
            mottakId = UUID.randomUUID(),
            avsluttetAvSaksbehandler = LocalDateTime.now()
        )

        assertThrows<KlagebehandlingAvsluttetException> { tilgangService.verifySaksbehandlersSkrivetilgang(klage) }
    }

    @Test
    fun `verifySaksbehandlersSkrivetilgang gir feil ved annen tildelt saksbehandler`() {
        val klage = Klagebehandling(
            versjon = 2L,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                skalMottaKopi = false
            ),
            tema = Tema.OMS,
            type = Type.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel.FTL_8_7
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            kildesystem = Fagsystem.FS39,
            mottakId = UUID.randomUUID(),
            tildeling = Tildeling(saksbehandlerident = "Z123456", enhet = "", tidspunkt = LocalDateTime.now())
        )

        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z654321")

        assertThrows<MissingTilgangException> { tilgangService.verifySaksbehandlersSkrivetilgang(klage) }
    }

    @Test
    fun `verifySaksbehandlersSkrivetilgang gir feil når ingen har tildelt`() {
        val klage = Klagebehandling(
            versjon = 2L,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                skalMottaKopi = false
            ),
            tema = Tema.OMS,
            type = Type.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel.FTL_8_7
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            kildesystem = Fagsystem.FS39,
            mottakId = UUID.randomUUID()
        )

        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z654321")

        assertThrows<MissingTilgangException> { tilgangService.verifySaksbehandlersSkrivetilgang(klage) }
    }

    @Test
    fun `verifySaksbehandlersSkrivetilgang gir ok ved samme ident`() {
        val klage = Klagebehandling(
            versjon = 2L,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                skalMottaKopi = false
            ),
            tema = Tema.OMS,
            type = Type.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel.FTL_8_7
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            kildesystem = Fagsystem.FS39,
            mottakId = UUID.randomUUID(),
            tildeling = Tildeling(saksbehandlerident = "Z123456", enhet = "", tidspunkt = LocalDateTime.now())
        )

        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z123456")

        assertThat(tilgangService.verifySaksbehandlersSkrivetilgang(klage)).isEqualTo(Unit)
    }

    @Test
    fun `verifySaksbehandlersSkrivetilgang gir ok for medunderskriver`() {
        val medunderskriverIdent = "Z654321"

        val klage = Klagebehandling(
            versjon = 2L,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                skalMottaKopi = false
            ),
            tema = Tema.OMS,
            type = Type.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel.FTL_8_7
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            kildesystem = Fagsystem.FS39,
            mottakId = UUID.randomUUID(),
            tildeling = Tildeling(saksbehandlerident = "Z123456", enhet = "", tidspunkt = LocalDateTime.now()),
            medunderskriver = MedunderskriverTildeling(saksbehandlerident = medunderskriverIdent, tidspunkt = LocalDateTime.now())
        )

        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns(medunderskriverIdent)

        assertThat(tilgangService.verifySaksbehandlersSkrivetilgang(klage)).isEqualTo(Unit)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir false på fortrolig`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.FORTROLIG,
                kjoenn = ""
            )
        )

        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() }.returns(false)
        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z123456")
        assertThat(tilgangService.harSaksbehandlerTilgangTil("")).isEqualTo(false)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir false på strengt fortrolig`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.STRENGT_FORTROLIG,
                kjoenn = ""
            )
        )

        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() }.returns(false)
        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z123456")
        assertThat(tilgangService.harSaksbehandlerTilgangTil("")).isEqualTo(false)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir false på egen ansatt`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = null,
                kjoenn = ""
            )
        )

        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() }.returns(false)
        every { innloggetSaksbehandlerRepository.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(true)
        assertThat(tilgangService.harSaksbehandlerTilgangTil("")).isEqualTo(false)
    }

}
