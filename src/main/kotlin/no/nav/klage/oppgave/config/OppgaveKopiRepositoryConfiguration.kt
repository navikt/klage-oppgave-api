package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.repositories.OppgaveKopiRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.incrementer.PostgresSequenceMaxValueIncrementer
import javax.sql.DataSource

@Configuration
class OppgaveKopiRepositoryConfiguration(val dataSource: DataSource, val jdbcTemplate: JdbcTemplate) {

    @Bean
    fun identIdIncrementer(): PostgresSequenceMaxValueIncrementer =
        PostgresSequenceMaxValueIncrementer(dataSource, "oppgave.ident_seq")

    @Bean
    fun versjonidentIdIncrementer(): PostgresSequenceMaxValueIncrementer =
        PostgresSequenceMaxValueIncrementer(dataSource, "oppgave.versjonident_seq")

    @Bean
    fun metadataIdIncrementer(): PostgresSequenceMaxValueIncrementer =
        PostgresSequenceMaxValueIncrementer(dataSource, "oppgave.metadata_seq")

    @Bean
    fun versjonMetadataIdIncrementer(): PostgresSequenceMaxValueIncrementer =
        PostgresSequenceMaxValueIncrementer(dataSource, "oppgave.versjonmetadata_seq")

    @Bean
    fun oppgaveKopiRepository(): OppgaveKopiRepository =
        OppgaveKopiRepository(
            dataSource,
            identIdIncrementer(),
            versjonidentIdIncrementer(),
            metadataIdIncrementer(),
            versjonMetadataIdIncrementer(),
            jdbcTemplate
        )
}