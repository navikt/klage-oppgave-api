package no.nav.klage.oppgave.domain.elasticsearch

import no.nav.klage.oppgave.domain.OppgaveListVisning
import org.elasticsearch.index.VersionType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDate
import java.time.LocalDateTime

@Document(indexName = "oppgavekopier", shards = 3, replicas = 2, versionType = VersionType.EXTERNAL)
data class EsOppgave(
    @Id
    override val id: Long,
    //Må være Long? for å bli Long på JVMen (isf long), og det krever Spring DataES..
    @Version
    val version: Long?,
    @Field(type = FieldType.Long)
    val journalpostId: String? = null,
    @Field(type = FieldType.Keyword)
    val saksreferanse: String? = null,
    @Field(type = FieldType.Keyword)
    val mappeId: Long? = null,
    @Field(type = FieldType.Keyword)
    val status: Status,
    @Field(type = FieldType.Keyword)
    override val tildeltEnhetsnr: String,
    @Field(type = FieldType.Keyword)
    val opprettetAvEnhetsnr: String? = null,
    @Field(type = FieldType.Keyword)
    val endretAvEnhetsnr: String? = null,
    @Field(type = FieldType.Keyword)
    val tema: String,
    @Field(type = FieldType.Keyword)
    val temagruppe: String? = null,
    @Field(type = FieldType.Keyword)
    val behandlingstema: String? = null,
    @Field(type = FieldType.Keyword)
    val oppgavetype: String,
    @Field(type = FieldType.Keyword)
    val behandlingstype: String? = null,
    @Field(type = FieldType.Keyword)
    val prioritet: Prioritet,
    @Field(type = FieldType.Keyword)
    override val tilordnetRessurs: String? = null,
    @Field(type = FieldType.Text)
    override val beskrivelse: String? = null,
    @Field(type = FieldType.Date, format = DateFormat.date)
    override val fristFerdigstillelse: LocalDate?,
    @Field(type = FieldType.Date, format = DateFormat.date)
    val aktivDato: LocalDate,
    @Field(type = FieldType.Keyword)
    val opprettetAv: String,
    @Field(type = FieldType.Keyword)
    val endretAv: String? = null,
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    val opprettetTidspunkt: LocalDateTime,
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    val endretTidspunkt: LocalDateTime? = null,
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    val ferdigstiltTidspunkt: LocalDateTime? = null,
    @Field(type = FieldType.Text)
    val behandlesAvApplikasjon: String? = null,
    @Field(type = FieldType.Keyword)
    val journalpostkilde: String? = null,
    @Field(type = FieldType.Keyword)
    val aktoerId: String? = null,
    @Field(type = FieldType.Keyword)
    override val fnr: String? = null,
    @Field(type = FieldType.Keyword)
    override val hjemler: List<String>? = null,
    @Field(type = FieldType.Keyword)
    val statuskategori: Statuskategori = status.kategoriForStatus(),
    @Field(type = FieldType.Boolean)
    val egenAnsatt: Boolean = false,
    @Field(type = FieldType.Keyword)
    override val type: String,
    @Field(type = FieldType.Keyword)
    override val ytelse: String
) : OppgaveListVisning {
    override val versjon: Int
        get() = version!!.toInt()

    override val statusString: String
        get() = status.name
    override val viktigsteHjemmel: String
        get() = hjemler?.firstOrNull() ?: "mangler"
}

