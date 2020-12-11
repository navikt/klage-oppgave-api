package no.nav.klage.oppgave.domain.elasticsearch

import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "oppgavekopier", shards = 3, replicas = 2)
class EsOppgave {
/*
    @Id
    val id: Long,
    val versjon: Int,
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
    val fristFerdigstillelse: LocalDate,
    val aktivDato: LocalDate,
    @Field(type = FieldType.Keyword)
    val opprettetAv: String,
    @Field(type = FieldType.Keyword)
    val endretAv: String? = null,
    val opprettetTidspunkt: LocalDateTime,
    val endretTidspunkt: LocalDateTime? = null,
    val ferdigstiltTidspunkt: LocalDateTime? = null,
    val behandlesAvApplikasjon: String? = null,
    val journalpostkilde: String? = null,
    @Field(type = FieldType.Keyword)
    val aktoerId: String? = null,
    @Field(type = FieldType.Keyword)
    val fnr: String? = null,
    val hjemler: List<String>? = null,
    val statuskategori(): String
*/
}