package no.nav.klage.oppgave.domain.elasticsearch

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
    val id: Long,
    //Må være Long? for å bli Long på JVMen (isf long), og det krever Spring DataES..
    @Version
    val versjon: Long,
    val journalpostId: String? = null,
    val saksreferanse: String? = null,
    val mappeId: Long? = null,
    @Field(type = FieldType.Keyword)
    val status: Status,
    @Field(type = FieldType.Keyword)
    val tildeltEnhetsnr: String,
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
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    @Field(type = FieldType.Date, format = DateFormat.date)
    val fristFerdigstillelse: LocalDate,
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
    val behandlesAvApplikasjon: String? = null,
    val journalpostkilde: String? = null,
    @Field(type = FieldType.Keyword)
    val aktoerId: String? = null,
    @Field(type = FieldType.Keyword)
    val fnr: String? = null,
    val hjemler: List<String>? = null,
    @Field(type = FieldType.Keyword)
    val statuskategori: Statuskategori = status.kategoriForStatus(),
    val egenAnsatt: Boolean = false
)

