package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.oppgavekopi.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("local")
@DataJpaTest
class OppgaveKopiRepositoryTest {

    @Autowired
    lateinit var oppgaveKopiRepository: OppgaveKopiRepository

    @Autowired
    lateinit var oppgaveKopiVersjonRepository: OppgaveKopiVersjonRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun oppgaveKopiWithOnlyMandatoryValuesShouldBeStoredProperly() {
        val oppgaveKopi = OppgaveKopi(
            id = 1001L,
            versjon = 1,
            tema = "tema",
            status = Status.OPPRETTET,
            tildeltEnhetsnr = "4219",
            oppgavetype = "KLAGE",
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = LocalDate.now(),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.now()
        )
        oppgaveKopiRepository.save(oppgaveKopi)
        oppgaveKopiVersjonRepository.save(oppgaveKopi.toVersjon())
        oppgaveKopiRepository.flush()
        oppgaveKopiVersjonRepository.flush()

        val oppgaveCount = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM oppgave.oppgave",
            emptyArray(),
            Integer::class.java
        )
        assertThat(oppgaveCount).isEqualTo(1)

        val oppgaveVersjonCount = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM oppgave.oppgaveversjon",
            emptyArray(),
            Integer::class.java
        )
        assertThat(oppgaveVersjonCount).isEqualTo(1)
    }

    @Test
    fun oppgaveKopiWithIdentShouldBeStoredProperly() {
        val oppgaveKopi = OppgaveKopi(
            id = 1001L,
            versjon = 1,
            tema = "tema",
            status = Status.OPPRETTET,
            tildeltEnhetsnr = "4219",
            oppgavetype = "KLAGE",
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = LocalDate.now(),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.now(),
            ident = Ident(
                id = null,
                identType = IdentType.AKTOERID,
                verdi = "12345",
                folkeregisterident = null,
                registrertDato = null
            )
        )
        oppgaveKopiRepository.save(oppgaveKopi)
        oppgaveKopiVersjonRepository.save(oppgaveKopi.toVersjon())
        oppgaveKopiRepository.flush()
        oppgaveKopiVersjonRepository.flush()

        val identCount = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM oppgave.ident",
            emptyArray(),
            Integer::class.java
        )
        assertThat(identCount).isEqualTo(1)
    }

    @Test
    fun oppgaveKopiWithMetadataShouldBeStoredProperly() {
        val oppgaveKopi = OppgaveKopi(
            id = 1001L,
            versjon = 1,
            tema = "tema",
            status = Status.OPPRETTET,
            tildeltEnhetsnr = "4219",
            oppgavetype = "KLAGE",
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = LocalDate.now(),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.now(),
            metadata = setOf(
                no.nav.klage.oppgave.domain.oppgavekopi.Metadata(
                    noekkel = MetadataNoekkel.HJEMMEL,
                    verdi = "8-25"
                )
            )
        )
        oppgaveKopiRepository.save(oppgaveKopi)
        oppgaveKopiVersjonRepository.save(oppgaveKopi.toVersjon())
        oppgaveKopiRepository.flush()
        oppgaveKopiVersjonRepository.flush()

        val metadataCount = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM oppgave.metadata",
            emptyArray(),
            Integer::class.java
        )
        assertThat(metadataCount).isEqualTo(1)

        val versjonMetadataCount = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM oppgave.versjonmetadata",
            emptyArray(),
            Integer::class.java
        )
        assertThat(versjonMetadataCount).isEqualTo(1)
    }

}