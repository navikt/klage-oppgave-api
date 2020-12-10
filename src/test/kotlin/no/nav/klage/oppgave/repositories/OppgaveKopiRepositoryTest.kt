package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.config.OppgaveKopiRepositoryConfiguration
import no.nav.klage.oppgave.domain.oppgavekopi.*
import no.nav.klage.oppgave.util.getLogger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("local")
@JdbcTest
@Import(OppgaveKopiRepositoryConfiguration::class)
class OppgaveKopiRepositoryTest(
    @Autowired val oppgaveKopiRepository: OppgaveKopiRepository,
    @Autowired val jdbcTemplate: JdbcTemplate
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Test
    fun oppgaveKopiWithOnlyMandatoryValuesShouldBeStoredProperly() {

        val now = LocalDateTime.now()
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
            opprettetTidspunkt = now
        )
        oppgaveKopiRepository.lagreOppgaveKopi(oppgaveKopi)

        val hentetOppgave = oppgaveKopiRepository.hentOppgaveKopi(oppgaveKopi.id)
        assertThat(hentetOppgave).isNotNull
        assertThat(hentetOppgave?.opprettetTidspunkt).isEqualTo(now)
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
            ident = Ident(IdentType.AKTOERID, "12345", null, null)
        )
        oppgaveKopiRepository.lagreOppgaveKopi(oppgaveKopi)

        val hentetOppgave = oppgaveKopiRepository.hentOppgaveKopi(oppgaveKopi.id)
        assertThat(hentetOppgave).isNotNull
        assertThat(hentetOppgave?.ident).isNotNull
        assertThat(hentetOppgave?.ident?.verdi).isEqualTo("12345")
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
            metadata = mapOf(MetadataNoekkel.HJEMMEL to "8-25")
        )
        oppgaveKopiRepository.lagreOppgaveKopi(oppgaveKopi)

        val hentetOppgave = oppgaveKopiRepository.hentOppgaveKopi(oppgaveKopi.id)
        assertThat(hentetOppgave).isNotNull
        assertThat(hentetOppgave?.metadata).isNotNull
        assertThat(hentetOppgave?.metadata?.size).isEqualTo(1)
        assertThat(hentetOppgave?.metadata?.get(MetadataNoekkel.HJEMMEL)).isEqualTo("8-25")

//        val versjonMetadataCount = jdbcTemplate.queryForObject(
//            "SELECT count(*) FROM oppgave.versjonmetadata",
//            emptyArray(),
//            Integer::class.java
//        )
//        assertThat(versjonMetadataCount).isEqualTo(1)
    }

    @Test
    fun twoVersionsOfOppgaveKopiShouldBeStoredProperly() {
        val oppgaveKopi1 = OppgaveKopi(
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
            ident = Ident(IdentType.AKTOERID, "12345", null, null),
            metadata = mapOf(MetadataNoekkel.HJEMMEL to "8-25")
        )
        val oppgaveKopi2 = OppgaveKopi(
            id = 1001L,
            versjon = 2,
            tema = "tema",
            status = Status.OPPRETTET,
            tildeltEnhetsnr = "4219",
            oppgavetype = "KLAGE",
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = LocalDate.now(),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.now(),
            ident = Ident(IdentType.AKTOERID, "12345", null, null),
            metadata = mapOf(MetadataNoekkel.HJEMMEL to "8-25")
        )
        oppgaveKopiRepository.lagreOppgaveKopi(oppgaveKopi1)
        oppgaveKopiRepository.lagreOppgaveKopi(oppgaveKopi2)
        val hentetOppgave = oppgaveKopiRepository.hentOppgaveKopi(oppgaveKopi1.id)
        assertThat(hentetOppgave).isNotNull
    }

    @Test
    fun storingTheSameOppgaveTwiceShouldNotCauseError() {
        val oppgaveKopi1 = OppgaveKopi(
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
            ident = Ident(IdentType.AKTOERID, "12345", null, null),
            metadata = mapOf(MetadataNoekkel.HJEMMEL to "8-25")
        )

        oppgaveKopiRepository.lagreOppgaveKopi(oppgaveKopi1)
        oppgaveKopiRepository.lagreOppgaveKopi(oppgaveKopi1)
        val hentetOppgave = oppgaveKopiRepository.hentOppgaveKopi(oppgaveKopi1.id)
        assertThat(hentetOppgave).isNotNull
    }

    @Test
    fun oppgaveversjonShouldBeStoredProperly() {
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
            ident = Ident(IdentType.AKTOERID, "12345", null, null),
            metadata = mapOf(MetadataNoekkel.HJEMMEL to "8-25")
        )
        oppgaveKopiRepository.lagreOppgaveKopiVersjon(oppgaveKopi)

        val hentetOppgaveversjon = oppgaveKopiRepository.hentOppgaveKopiVersjon(oppgaveKopi.id, oppgaveKopi.versjon)
        assertThat(hentetOppgaveversjon).isNotNull
        assertThat(hentetOppgaveversjon!!.opprettetTidspunkt).isEqualTo(oppgaveKopi.opprettetTidspunkt)

        val hentetOppgaveSisteVersjon =
            oppgaveKopiRepository.hentOppgaveKopiSisteVersjon(oppgaveKopi.id)
        assertThat(hentetOppgaveSisteVersjon).isNotNull
        assertThat(hentetOppgaveSisteVersjon!!.opprettetTidspunkt).isEqualTo(oppgaveKopi.opprettetTidspunkt)
    }

    @Test
    fun storingTheSameOppgaveversjonTwiceShouldNotCauseError() {
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
            ident = Ident(IdentType.AKTOERID, "12345", null, null),
            metadata = mapOf(MetadataNoekkel.HJEMMEL to "8-25")
        )
        oppgaveKopiRepository.lagreOppgaveKopiVersjon(oppgaveKopi)
        oppgaveKopiRepository.lagreOppgaveKopiVersjon(oppgaveKopi)

        val hentetOppgaveversjon = oppgaveKopiRepository.hentOppgaveKopiVersjon(oppgaveKopi.id, oppgaveKopi.versjon)
        assertThat(hentetOppgaveversjon).isNotNull
    }

    @Test
    fun storingTwoOppgaveversjonsShouldWorkProperly() {
        val oppgaveKopi1 = OppgaveKopi(
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
            ident = Ident(IdentType.AKTOERID, "12345", null, null),
            metadata = mapOf(MetadataNoekkel.HJEMMEL to "8-25")
        )
        val oppgaveKopi2 = OppgaveKopi(
            id = 1001L,
            versjon = 2,
            tema = "tema",
            status = Status.OPPRETTET,
            tildeltEnhetsnr = "4219",
            oppgavetype = "KLAGE",
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = LocalDate.now(),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.now(),
            ident = Ident(IdentType.AKTOERID, "12345", null, null),
            metadata = mapOf(MetadataNoekkel.HJEMMEL to "8-25")
        )
        oppgaveKopiRepository.lagreOppgaveKopiVersjon(oppgaveKopi2)
        oppgaveKopiRepository.lagreOppgaveKopiVersjon(oppgaveKopi1)

        val hentetOppgaveversjon = oppgaveKopiRepository.hentOppgaveKopiVersjon(oppgaveKopi1.id, oppgaveKopi1.versjon)
        assertThat(hentetOppgaveversjon).isNotNull
        assertThat(hentetOppgaveversjon!!.opprettetTidspunkt).isEqualTo(oppgaveKopi1.opprettetTidspunkt)

        val hentetOppgaveSisteVersjon =
            oppgaveKopiRepository.hentOppgaveKopiSisteVersjon(oppgaveKopi2.id)
        assertThat(hentetOppgaveSisteVersjon).isNotNull
        assertThat(hentetOppgaveSisteVersjon!!.opprettetTidspunkt).isEqualTo(oppgaveKopi2.opprettetTidspunkt)

    }
}