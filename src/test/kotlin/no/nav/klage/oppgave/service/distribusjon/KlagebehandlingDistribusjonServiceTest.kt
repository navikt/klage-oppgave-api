package no.nav.klage.oppgave.service.distribusjon

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import no.nav.klage.oppgave.clients.dokdistfordeling.DistribuerJournalpostResponse
import no.nav.klage.oppgave.clients.dokdistfordeling.DokDistFordelingClient
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.*
import no.nav.klage.oppgave.repositories.KafkaVedtakEventRepository
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.repositories.MottakRepository
import no.nav.klage.oppgave.service.DokumentService
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.TilgangService
import no.nav.klage.oppgave.service.VedtakKafkaProducer
import no.nav.klage.oppgave.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


@ActiveProfiles("local")
@Import(KlagebehandlingDistribusjonServiceTest.MyTestConfiguration::class)
@SpringBootTest(classes = [KlagebehandlingService::class, VedtakDistribusjonService::class, KlagebehandlingDistribusjonService::class])
@EnableJpaRepositories(basePackages = ["no.nav.klage.oppgave.repositories"])
@EntityScan("no.nav.klage.oppgave.domain")
@AutoConfigureDataJpa
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@AutoConfigureTestEntityManager
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class KlagebehandlingDistribusjonServiceTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Configuration
    internal class MyTestConfiguration {

        @MockkBean(relaxed = true)
        lateinit var tilgangService: TilgangService

        @MockkBean(relaxed = true)
        lateinit var applicationEventPublisher: ApplicationEventPublisher

        @MockkBean(relaxed = true)
        lateinit var dokumentService: DokumentService

        @MockkBean(relaxed = true)
        lateinit var tokenUtil: TokenUtil

        @MockkBean(relaxed = true)
        lateinit var vedtakKafkaProducer: VedtakKafkaProducer

    }

    //@Autowired
    //lateinit var entityManager: EntityManager
    //lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var klagebehandlingRepository: KlagebehandlingRepository

    @Autowired
    lateinit var mottakRepository: MottakRepository

    @Autowired
    lateinit var klagebehandlingDistribusjonService: KlagebehandlingDistribusjonService

    @MockkBean
    lateinit var dokDistFordelingClient: DokDistFordelingClient

    private val klagebehandlingId = UUID.randomUUID()

    private val vedtakId = UUID.randomUUID()

    private val fnr = "12345678910"

    @SpykBean
    lateinit var vedtakDistribusjonService: VedtakDistribusjonService

    @SpykBean
    lateinit var kafkaVedtakEventRepository: KafkaVedtakEventRepository

    @SpykBean
    lateinit var klagebehandlingAvslutningService: KlagebehandlingAvslutningService

    private val mottak = Mottak(
        tema = Tema.OMS,
        type = Type.KLAGE,
        kildesystem = Fagsystem.FS39,
        kildeReferanse = "1234234",
        klager = Klager(partId = PartId(type = PartIdType.PERSON, value = fnr)),
        oversendtKaDato = LocalDateTime.now()
    )

    private val klage = Klagebehandling(
        id = klagebehandlingId,
        klager = Klager(partId = PartId(type = PartIdType.PERSON, value = fnr)),
        sakenGjelder = SakenGjelder(
            partId = PartId(type = PartIdType.PERSON, value = fnr),
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
        mottakId = mottak.id,
        vedtak = mutableSetOf(
            Vedtak(
                id = vedtakId,
                journalpostId = "1",
                utfall = Utfall.MEDHOLD
            )
        )
    )

    @Test
    fun `save klagebehandling`() {
        mottakRepository.save(mottak)

        klagebehandlingRepository.save(klage)

        klagebehandlingDistribusjonService.distribuerKlagebehandling(klagebehandlingId)

        val klagebehandling =
            klagebehandlingRepository.findByIdOrNull(klagebehandlingId) ?: throw NullPointerException()
        println(klagebehandling.versjon)
    }

    @Test
    fun `distribusjon av klagebehandling fører til dokdistReferanse, ferdig distribuert vedtak og avsluttet klagebehandling`() {
        val dokdistResponse = DistribuerJournalpostResponse(UUID.randomUUID())

        every { dokDistFordelingClient.distribuerJournalpost(any())  } returns dokdistResponse

        mottakRepository.save(mottak)

        klagebehandlingRepository.save(klage)

        klagebehandlingDistribusjonService.distribuerKlagebehandling(klagebehandlingId)

        val result = klagebehandlingRepository.getOne(klagebehandlingId)
        val resultingVedtak = result.getVedtak(vedtakId)
        val brevMottakere = resultingVedtak.brevmottakere

        assertThat(brevMottakere.first().dokdistReferanse).isNotNull
        assertThat(resultingVedtak.ferdigDistribuert).isNotNull
        assertThat(result.avsluttet).isNotNull
    }
}