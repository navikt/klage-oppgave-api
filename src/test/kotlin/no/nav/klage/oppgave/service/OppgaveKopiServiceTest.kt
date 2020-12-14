package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.oppgavekopi.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("local")
@DataJpaTest
@Import(OppgaveKopiService::class)
class OppgaveKopiServiceTest {

    @Autowired
    lateinit var oppgaveKopiService: OppgaveKopiService

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
        oppgaveKopiService.lagreOppgaveKopi(oppgaveKopi)

        val hentetOppgave = oppgaveKopiService.hentOppgaveKopi(oppgaveKopi.id)
        assertThat(hentetOppgave).isNotNull
        assertThat(hentetOppgave.opprettetTidspunkt).isEqualTo(now)
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
            ident = Ident(null, IdentType.AKTOERID, "12345", null, null)
        )
        oppgaveKopiService.lagreOppgaveKopi(oppgaveKopi)

        val hentetOppgave = oppgaveKopiService.hentOppgaveKopi(oppgaveKopi.id)
        assertThat(hentetOppgave).isNotNull
        assertThat(hentetOppgave.ident).isNotNull
        assertThat(hentetOppgave.ident?.verdi).isEqualTo("12345")
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
            metadata = setOf(no.nav.klage.oppgave.domain.oppgavekopi.Metadata(null, MetadataNoekkel.HJEMMEL, "8-25"))
        )
        oppgaveKopiService.lagreOppgaveKopi(oppgaveKopi)

        val hentetOppgave = oppgaveKopiService.hentOppgaveKopi(oppgaveKopi.id)
        assertThat(hentetOppgave).isNotNull
        assertThat(hentetOppgave.metadata).isNotNull
        assertThat(hentetOppgave.metadata.size).isEqualTo(1)
        assertThat(hentetOppgave.metadataAsMap()[MetadataNoekkel.HJEMMEL]).isEqualTo("8-25")

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
            ident = Ident(null, IdentType.AKTOERID, "12345", null, null),
            metadata = setOf(no.nav.klage.oppgave.domain.oppgavekopi.Metadata(null, MetadataNoekkel.HJEMMEL, "8-25"))
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
            ident = Ident(null, IdentType.AKTOERID, "12345", null, null),
            metadata = setOf(no.nav.klage.oppgave.domain.oppgavekopi.Metadata(null, MetadataNoekkel.HJEMMEL, "8-25"))
        )
        oppgaveKopiService.lagreOppgaveKopi(oppgaveKopi1)
        oppgaveKopiService.lagreOppgaveKopi(oppgaveKopi2)
        val hentetOppgave = oppgaveKopiService.hentOppgaveKopi(oppgaveKopi1.id)
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
            ident = Ident(null, IdentType.AKTOERID, "12345", null, null),
            metadata = setOf(no.nav.klage.oppgave.domain.oppgavekopi.Metadata(null, MetadataNoekkel.HJEMMEL, "8-25"))
        )

        oppgaveKopiService.lagreOppgaveKopi(oppgaveKopi1)
        oppgaveKopiService.lagreOppgaveKopi(oppgaveKopi1)
        val hentetOppgave = oppgaveKopiService.hentOppgaveKopi(oppgaveKopi1.id)
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
            ident = Ident(null, IdentType.AKTOERID, "12345", null, null),
            metadata = setOf(no.nav.klage.oppgave.domain.oppgavekopi.Metadata(null, MetadataNoekkel.HJEMMEL, "8-25"))
        )
        oppgaveKopiService.lagreOppgaveKopi(oppgaveKopi)

        val hentetOppgaveversjon = oppgaveKopiService.hentOppgaveKopiVersjon(oppgaveKopi.id, oppgaveKopi.versjon)
        assertThat(hentetOppgaveversjon).isNotNull
        assertThat(hentetOppgaveversjon.opprettetTidspunkt).isEqualTo(oppgaveKopi.opprettetTidspunkt)

        val hentetOppgaveSisteVersjon =
            oppgaveKopiService.hentOppgaveKopiSisteVersjon(oppgaveKopi.id)
        assertThat(hentetOppgaveSisteVersjon).isNotNull
        assertThat(hentetOppgaveSisteVersjon.opprettetTidspunkt).isEqualTo(oppgaveKopi.opprettetTidspunkt)
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
            ident = Ident(null, IdentType.AKTOERID, "12345", null, null),
            metadata = setOf(no.nav.klage.oppgave.domain.oppgavekopi.Metadata(null, MetadataNoekkel.HJEMMEL, "8-25"))
        )
        oppgaveKopiService.lagreOppgaveKopi(oppgaveKopi)

        val hentetOppgaveversjon = oppgaveKopiService.hentOppgaveKopiVersjon(oppgaveKopi.id, oppgaveKopi.versjon)
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
            ident = Ident(null, IdentType.AKTOERID, "12345", null, null),
            metadata = setOf(no.nav.klage.oppgave.domain.oppgavekopi.Metadata(null, MetadataNoekkel.HJEMMEL, "8-25"))
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
            ident = Ident(null, IdentType.AKTOERID, "12345", null, null),
            metadata = setOf(no.nav.klage.oppgave.domain.oppgavekopi.Metadata(null, MetadataNoekkel.HJEMMEL, "8-25"))
        )
        oppgaveKopiService.lagreOppgaveKopi(oppgaveKopi2)
        oppgaveKopiService.lagreOppgaveKopi(oppgaveKopi1)

        val hentetOppgaveversjon = oppgaveKopiService.hentOppgaveKopiVersjon(oppgaveKopi1.id, oppgaveKopi1.versjon)
        assertThat(hentetOppgaveversjon).isNotNull
        assertThat(hentetOppgaveversjon.opprettetTidspunkt).isEqualTo(oppgaveKopi1.opprettetTidspunkt)

        val hentetOppgaveSisteVersjon =
            oppgaveKopiService.hentOppgaveKopiSisteVersjon(oppgaveKopi2.id)
        assertThat(hentetOppgaveSisteVersjon).isNotNull
        assertThat(hentetOppgaveSisteVersjon.opprettetTidspunkt).isEqualTo(oppgaveKopi2.opprettetTidspunkt)

    }

}