package no.nav.klage.oppgave.db

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.test.context.ActiveProfiles
import java.sql.ResultSet

@ActiveProfiles("local")
@DataJpaTest
@Disabled
class FlywayMigrationTest {

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    data class Status(val id: Long, val navn: String, val kategori: String)

    @Test
    fun flyway_should_run() {
        val statuser: List<Status> = jdbcTemplate.query(
            "SELECT * FROM oppgave.status",
            emptyArray(),
            RowMapper { rs: ResultSet, _: Int ->
                Status(
                    rs.getLong("id"),
                    rs.getString("navn"),
                    rs.getString("kategori")
                )
            })

        assertThat(statuser).hasSize(5)
    }

}