package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KlagebehandlingRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var klagebehandlingRepository: KlagebehandlingRepository

    @Autowired
    lateinit var mottakRepository: MottakRepository


    @Test
    fun `persist klage works`() {

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

        assertThat(klagebehandlingRepository.findById(klage.id).get()).isEqualTo(klage)
    }

    @Test
    fun `persist klage with saksdokumenter works`() {

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
            saksdokumenter = mutableSetOf(
                Saksdokument(journalpostId = "REF1", dokumentInfoId = "123"),
                Saksdokument(journalpostId = "REF2", dokumentInfoId = "321"),
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

        assertThat(klagebehandlingRepository.findById(klage.id).get()).isEqualTo(klage)
    }

    @Test
    fun `remove saksdokument on saved klage works`() {

        testEntityManager.flush()
        testEntityManager.clear()

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
            saksdokumenter = mutableSetOf(
                Saksdokument(journalpostId = "REF1", dokumentInfoId = "123"),
                Saksdokument(journalpostId = "REF2", dokumentInfoId = "321"),
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

        val foundklage = klagebehandlingRepository.findById(klage.id).get()
        foundklage.saksdokumenter.removeIf { it.journalpostId == "REF1" }

        testEntityManager.flush()
        testEntityManager.clear()

        val foundModifiedKlage = klagebehandlingRepository.findById(klage.id).get()
        assertThat(foundModifiedKlage.saksdokumenter).hasSize(1)
        assertThat(foundModifiedKlage.saksdokumenter.first().journalpostId).isEqualTo("REF2")
    }

}
