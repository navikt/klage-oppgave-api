package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.oppgavekopi.*
import no.nav.klage.oppgave.util.getLogger
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.jdbc.support.incrementer.PostgresSequenceMaxValueIncrementer
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import javax.sql.DataSource

open class OppgaveKopiRepository(
    val dataSource: DataSource,
    val identIdIncrementer: PostgresSequenceMaxValueIncrementer,
    val versjonidentIdIncrementer: PostgresSequenceMaxValueIncrementer,
    val metadataIdIncrementer: PostgresSequenceMaxValueIncrementer,
    val versjonMetadataIdIncrementer: PostgresSequenceMaxValueIncrementer,
    val jdbcTemplate: JdbcTemplate
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    private val employeeExtractor: (ResultSet) -> OppgaveKopi? = { rs ->
        val metadata: MutableMap<MetadataNoekkel, String> = mutableMapOf()
        var ident: Ident?
        var oppgave: OppgaveKopi? = null

        while (rs.next()) {
            ident = extractIdent(rs)

            extractMetadata(rs)?.let { metadata.plusAssign(it) }

            val id: Long = rs.getLong("id")
            oppgave = OppgaveKopi(
                id = id,
                versjon = rs.getInt("versjon"),
                journalpostId = rs.getString("journalpostid"),
                saksreferanse = rs.getString("saksreferanse"),
                mappeId = rs.getLong("mappe_id").nullIfZero(),
                status = Status.fraStatusId(rs.getLong("status_id")),
                tildeltEnhetsnr = rs.getString("tildelt_enhetsnr"),
                opprettetAvEnhetsnr = rs.getString("opprettet_av_enhetsnr"),
                endretAvEnhetsnr = rs.getString("endret_av_enhetsnr"),
                tema = rs.getString("tema"),
                temagruppe = rs.getString("temagruppe"),
                behandlingstema = rs.getString("behandlingstema"),
                oppgavetype = rs.getString("oppgavetype"),
                behandlingstype = rs.getString("behandlingstype"),
                prioritet = Prioritet.valueOf(rs.getString("prioritet")),
                tilordnetRessurs = rs.getString("tilordnet_ressurs"),
                beskrivelse = rs.getString("beskrivelse"),
                fristFerdigstillelse = rs.getObject("frist_ferdigstillelse", LocalDate::class.java),
                aktivDato = rs.getObject("aktiv_dato", LocalDate::class.java),
                opprettetAv = rs.getString("opprettet_av"),
                endretAv = rs.getString("endret_av"),
                opprettetTidspunkt = rs.getObject("opprettet_tidspunkt", LocalDateTime::class.java),
                endretTidspunkt = rs.getObject("endret_tidspunkt", LocalDateTime::class.java),
                ferdigstiltTidspunkt = rs.getObject("ferdigstilt_tidspunkt", LocalDateTime::class.java),
                behandlesAvApplikasjon = rs.getString("behandles_av_applikasjon"),
                journalpostkilde = rs.getString("journalpostkilde"),
                ident = ident,
                metadata = metadata
            )
        }
        oppgave
    }

    private fun extractMetadata(rs: ResultSet): Pair<MetadataNoekkel, String>? {
        val noekkel = rs.getString("nokkel")
        val verdi = rs.getString("verdi")
        return if (noekkel != null) {
            MetadataNoekkel.valueOf(noekkel) to verdi
        } else {
            null
        }
    }

    private fun extractIdent(rs: ResultSet): Ident? {
        val type = rs.getString("TYPE")
        val identverdi = rs.getString("identverdi")
        val folkeregisterident = rs.getString("folkeregisterident")
        val registrertDato = rs.getObject("registrert_dato", LocalDate::class.java)
        return if (type != null) {
            Ident(
                identType = IdentType.valueOf(type),
                verdi = identverdi,
                folkeregisterident = folkeregisterident,
                registrertDato = registrertDato
            )
        } else {
            null
        }
    }

    private fun identParameterMap(ident: Ident, identId: Long) =
        mapOf(
            "id" to identId,
            "TYPE" to ident.identType.name,
            "verdi" to ident.verdi,
            "folkeregisterident" to ident.folkeregisterident,
            "registrert_dato" to ident.registrertDato
        )

    private fun metadataParameterMap(oppgaveKopi: OppgaveKopi, noekkel: MetadataNoekkel, verdi: String) =
        mapOf(
            "id" to metadataIdIncrementer.nextLongValue(),
            "oppgave_id" to oppgaveKopi.id,
            "nokkel" to noekkel.name,
            "verdi" to verdi,
        )

    private fun versjonMetadataParameterMap(oppgaveKopi: OppgaveKopi, noekkel: MetadataNoekkel, verdi: String) =
        mapOf(
            "id" to versjonMetadataIdIncrementer.nextLongValue(),
            "oppgave_id" to oppgaveKopi.id,
            "oppgave_versjon" to oppgaveKopi.versjon,
            "nokkel" to noekkel.name,
            "verdi" to verdi
        )

    private fun oppgaveParameterMap(oppgaveKopi: OppgaveKopi, identId: Long?) =
        mapOf(
            "id" to oppgaveKopi.id,
            "versjon" to oppgaveKopi.versjon,
            "journalpostid" to oppgaveKopi.journalpostId,
            "saksreferanse" to oppgaveKopi.saksreferanse,
            "mappe_id" to oppgaveKopi.mappeId,
            "status_id" to oppgaveKopi.status.statusId,
            "tildelt_enhetsnr" to oppgaveKopi.tildeltEnhetsnr,
            "opprettet_av_enhetsnr" to oppgaveKopi.opprettetAvEnhetsnr,
            "endret_av_enhetsnr" to oppgaveKopi.endretAvEnhetsnr,
            "tema" to oppgaveKopi.tema,
            "temagruppe" to oppgaveKopi.temagruppe,
            "behandlingstema" to oppgaveKopi.behandlingstema,
            "oppgavetype" to oppgaveKopi.oppgavetype,
            "behandlingstype" to oppgaveKopi.behandlingstype,
            "prioritet" to oppgaveKopi.prioritet.name,
            "tilordnet_ressurs" to oppgaveKopi.tilordnetRessurs,
            "beskrivelse" to oppgaveKopi.beskrivelse,
            "frist_ferdigstillelse" to oppgaveKopi.fristFerdigstillelse,
            "aktiv_dato" to oppgaveKopi.aktivDato,
            "opprettet_av" to oppgaveKopi.opprettetAv,
            "endret_av" to oppgaveKopi.endretAv,
            "opprettet_tidspunkt" to oppgaveKopi.opprettetTidspunkt,
            "endret_tidspunkt" to oppgaveKopi.endretTidspunkt,
            "ferdigstilt_tidspunkt" to oppgaveKopi.ferdigstiltTidspunkt,
            "behandles_av_applikasjon" to oppgaveKopi.behandlesAvApplikasjon,
            "journalpostkilde" to oppgaveKopi.journalpostkilde,
            "ident_id" to identId
        )

    val oppgaveJdbcInsert = SimpleJdbcInsert(dataSource).withSchemaName("oppgave").withTableName("oppgave")
    val oppgaveVersjonJdbcInsert =
        SimpleJdbcInsert(dataSource).withSchemaName("oppgave").withTableName("oppgaveversjon")
    val metadataJdbcInsert = SimpleJdbcInsert(dataSource).withSchemaName("oppgave").withTableName("metadata")
    val versjonMetadataJdbcInsert =
        SimpleJdbcInsert(dataSource).withSchemaName("oppgave").withTableName("versjonmetadata")
    val identJdbcInsert = SimpleJdbcInsert(dataSource).withSchemaName("oppgave").withTableName("ident")
    val versjonidentJdbcInsert = SimpleJdbcInsert(dataSource).withSchemaName("oppgave").withTableName("versjonident")

    private fun lagreIdentAndReturnId(ident: Ident?): Long? {
        return ident?.let {
            val identId = identIdIncrementer.nextLongValue()
            val identParameterMap = identParameterMap(it, identId)
            identJdbcInsert.execute(identParameterMap)
            identId
        }
    }

    private fun lagreVersjonidentAndReturnId(ident: Ident?): Long? {
        return ident?.let {
            val identId = versjonidentIdIncrementer.nextLongValue()
            val identParameterMap = identParameterMap(it, identId)
            versjonidentJdbcInsert.execute(identParameterMap)
            identId
        }
    }

    private fun deleteOppgave(oppgaveId: Long) {
        jdbcTemplate.update("DELETE FROM oppgave.metadata WHERE oppgave_id = ?", oppgaveId)
        jdbcTemplate.update("DELETE FROM oppgave.oppgave WHERE id = ?", oppgaveId)
        jdbcTemplate.update(
            "DELETE FROM oppgave.ident WHERE id IN( SELECT ident_id FROM oppgave.oppgave WHERE id = ?)",
            oppgaveId
        )
    }

    private fun insertOppgave(oppgaveKopi: OppgaveKopi) {
        val identId = lagreIdentAndReturnId(oppgaveKopi.ident)
        oppgaveJdbcInsert.execute(oppgaveParameterMap(oppgaveKopi, identId))
        oppgaveKopi.metadata?.forEach { metadataNoekkel, verdi ->
            metadataJdbcInsert.execute(metadataParameterMap(oppgaveKopi, metadataNoekkel, verdi))
        }
    }

    @Transactional
    open fun oppgaveExists(oppgaveId: Long): Boolean {
        return jdbcTemplate.queryForObject(
            "SELECT EXISTS(SELECT FROM oppgave.oppgave WHERE id = ?)",
            Boolean::class.java,
            oppgaveId
        )
    }

    @Transactional
    open fun oppgaveVersjonExists(oppgaveId: Long, versjon: Int): Boolean {
        return jdbcTemplate.queryForObject(
            "SELECT EXISTS(SELECT FROM oppgave.oppgaveversjon WHERE id = ? AND versjon = ?)",
            Boolean::class.java,
            oppgaveId, versjon
        )
    }

    @Transactional
    open fun hentOppgaveKopi(oppgaveId: Long): OppgaveKopi? {

        val query =
            "SELECT o.*, m.nokkel, m.verdi, i.TYPE, i.verdi AS identverdi, i.folkeregisterident, i.registrert_dato FROM oppgave.oppgave o " +
                    "LEFT JOIN oppgave.ident i ON o.ident_id = i.id " +
                    "LEFT JOIN oppgave.metadata m ON o.id = m.oppgave_id " +
                    "WHERE o.ID = ?"
        return jdbcTemplate.query(
            query,
            arrayOf(oppgaveId),
            employeeExtractor
        )
    }

    @Transactional
    open fun hentOppgaveKopiSisteVersjon(oppgaveId: Long): OppgaveKopi? {
        //Strengt tatt, så er dette det samme som hentOppgaveKopi..

        val query =
            "SELECT o.*, m.nokkel, m.verdi, i.TYPE, i.verdi AS identverdi, i.folkeregisterident, i.registrert_dato FROM oppgave.oppgaveversjon o " +
                    "LEFT JOIN oppgave.versjonident i ON o.ident_id = i.id " +
                    "LEFT JOIN oppgave.versjonmetadata m ON o.id = m.oppgave_id " +
                    "WHERE o.id = ? AND o.versjon = (SELECT MAX(versjon) FROM oppgave.oppgaveversjon o WHERE o.id = ?)"
        return jdbcTemplate.query(
            query,
            arrayOf(oppgaveId, oppgaveId),
            employeeExtractor
        )
    }

    @Transactional
    open fun hentOppgaveKopiVersjon(oppgaveId: Long, versjon: Int): OppgaveKopi? {

        val query =
            "SELECT o.*, m.nokkel, m.verdi, i.TYPE, i.verdi AS identverdi, i.folkeregisterident, i.registrert_dato FROM oppgave.oppgaveversjon o " +
                    "LEFT JOIN oppgave.versjonident i ON o.ident_id = i.id " +
                    "LEFT JOIN oppgave.versjonmetadata m ON o.id = m.oppgave_id " +
                    "WHERE o.id = ? AND o.versjon = ?"
        return jdbcTemplate.query(
            query,
            arrayOf(oppgaveId, versjon),
            employeeExtractor
        )
    }

    @Transactional
    open fun lagreOppgaveKopi(oppgaveKopi: OppgaveKopi) {
        val lagretOppgave = hentOppgaveKopi(oppgaveKopi.id)
        when {
            lagretOppgave == null -> {
                logger.debug("Ingen lagret oppgave fra før, inserter en ny")
                insertOppgave(oppgaveKopi)
            }
            lagretOppgave.versjon < oppgaveKopi.versjon -> {
                logger.debug("Eldre versjon av oppgaven fra før, deleter og inserter en ny")
                deleteOppgave(lagretOppgave.id)
                insertOppgave(oppgaveKopi)
            }
            lagretOppgave.versjon >= oppgaveKopi.versjon -> {
                logger.debug("Nyere eller lik versjon av oppgaven fra før, gjør ingenting")
                //Nyere versjon finnes allerede
            }
        }
    }

    @Transactional
    open fun lagreOppgaveKopiVersjon(oppgaveKopi: OppgaveKopi) {
        if (!oppgaveVersjonExists(oppgaveKopi.id, oppgaveKopi.versjon)) {
            val versjonidentId = lagreVersjonidentAndReturnId(oppgaveKopi.ident)
            oppgaveVersjonJdbcInsert.execute(oppgaveParameterMap(oppgaveKopi, versjonidentId))
            oppgaveKopi.metadata?.forEach { metadataNoekkel, verdi ->
                versjonMetadataJdbcInsert.execute(versjonMetadataParameterMap(oppgaveKopi, metadataNoekkel, verdi))
            }
        }
    }
}

private fun Long.nullIfZero(): Long? =
    if (this.equals(0)) {
        null
    } else {
        this
    }
