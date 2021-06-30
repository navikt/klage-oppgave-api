package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.*
import no.nav.klage.oppgave.exceptions.KlagebehandlingAvsluttetException
import no.nav.klage.oppgave.exceptions.KlagebehandlingSamtidigEndretException
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.repositories.MottakRepository
import no.nav.klage.oppgave.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KlagebehandlingServiceTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var mottakRepository: MottakRepository

    @Autowired
    lateinit var klagebehandlingRepository: KlagebehandlingRepository

    private val tilgangService: TilgangService = mockk()

    private val applicationEventPublisher: ApplicationEventPublisher = mockk()

    private val dokumentService: DokumentService = mockk()

    private val tokenUtil: TokenUtil = mockk()

    lateinit var klagebehandlingService: KlagebehandlingService

    @BeforeEach
    fun setup() {
        klagebehandlingService = KlagebehandlingService(
            klagebehandlingRepository,
            tilgangService,
            applicationEventPublisher,
            dokumentService,
            tokenUtil
        )
    }

    @Test
    fun `getKlagebehandlingForUpdate ok`() {
        val klage = simpleInsert()

        every { tilgangService.verifyInnloggetSaksbehandlersTilgangTil(any()) } returns Unit
        every { tilgangService.verifyInnloggetSaksbehandlersTilgangTilTema(any()) } returns Unit

        assertThat(
            klagebehandlingService.getKlagebehandlingForUpdate(
                klagebehandlingId = klage.id,
                ignoreCheckSkrivetilgang = true
            )
        ).isEqualTo(klage)
    }

    @Test
    fun `getKlagebehandlingForUpdate slår til på optimistic locking`() {
        val klage = simpleInsert()

        every { tilgangService.verifyInnloggetSaksbehandlersTilgangTil(any()) } returns Unit
        every { tilgangService.verifyInnloggetSaksbehandlersTilgangTilTema(any()) } returns Unit

        assertThrows<KlagebehandlingSamtidigEndretException> {
            klagebehandlingService.getKlagebehandlingForUpdate(
                klage.id,
                1L,
                true
            )
        }
    }

    @Test
    fun `getKlagebehandlingForUpdate sjekker skrivetilgang, fanger riktig exception`() {
        val klage = simpleInsert()

        every { tilgangService.verifyInnloggetSaksbehandlersTilgangTil(any()) } returns Unit
        every { tilgangService.verifyInnloggetSaksbehandlersTilgangTilTema(any()) } returns Unit
        every { tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(klage) }.throws(
            KlagebehandlingAvsluttetException("")
        )

        assertThrows<KlagebehandlingAvsluttetException> { klagebehandlingService.getKlagebehandlingForUpdate(klage.id) }
    }


    private fun simpleInsert(): Klagebehandling {
        val mottak = Mottak(
            tema = Tema.OMS,
            type = Type.KLAGE,
            kildesystem = Fagsystem.K9,
            kildeReferanse = "1234234",
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            oversendtKaDato = LocalDateTime.now()
        )

        mottakRepository.save(mottak)

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
            kildesystem = Fagsystem.K9,
            mottakId = mottak.id
        )

        klagebehandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        return klage
    }

}
