package no.nav.klage.oppgave.domain.oppgavekopi

import no.nav.klage.oppgave.domain.oppgavekopi.helper.StatusConverter
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "oppgaveversjon", schema = "oppgave")
@IdClass(OppgaveKopiVersjonId::class)
class OppgaveKopiVersjon(

    @Id
    @Column(name = "id")
    val id: Long,
    @Id
    @Column(name = "versjon")
    val versjon: Int,
    @Column(name = "journalpostid")
    val journalpostId: String? = null,
    @Column(name = "saksreferanse")
    val saksreferanse: String? = null,
    @Column(name = "mappe_id")
    val mappeId: Long? = null,
    @Column(name = "status_id")
    @Convert(converter = StatusConverter::class)
    val status: Status,
    @Column(name = "tildelt_enhetsnr")
    val tildeltEnhetsnr: String,
    @Column(name = "opprettet_av_enhetsnr")
    val opprettetAvEnhetsnr: String? = null,
    @Column(name = "endret_av_enhetsnr")
    val endretAvEnhetsnr: String? = null,
    @Column(name = "tema")
    val tema: String,
    @Column(name = "temagruppe")
    val temagruppe: String? = null,
    @Column(name = "behandlingstema")
    val behandlingstema: String? = null,
    @Column(name = "oppgavetype")
    val oppgavetype: String,
    @Column(name = "behandlingstype")
    val behandlingstype: String? = null,
    @Column(name = "prioritet")
    @Enumerated(EnumType.STRING)
    val prioritet: Prioritet,
    @Column(name = "tilordnet_ressurs")
    val tilordnetRessurs: String? = null,
    @Column(name = "beskrivelse")
    val beskrivelse: String? = null,
    @Column(name = "frist_ferdigstillelse")
    val fristFerdigstillelse: LocalDate?,
    @Column(name = "aktiv_dato")
    val aktivDato: LocalDate,
    @Column(name = "opprettet_av")
    val opprettetAv: String,
    @Column(name = "endret_av")
    val endretAv: String? = null,
    @Column(name = "opprettet_tidspunkt")
    val opprettetTidspunkt: LocalDateTime,
    @Column(name = "endret_tidspunkt")
    val endretTidspunkt: LocalDateTime? = null,
    @Column(name = "ferdigstilt_tidspunkt")
    val ferdigstiltTidspunkt: LocalDateTime? = null,
    @Column(name = "behandles_av_applikasjon")
    val behandlesAvApplikasjon: String? = null,
    @Column(name = "journalpostkilde")
    val journalpostkilde: String? = null,
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "ident_id", referencedColumnName = "id")
    val ident: VersjonIdent? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumns(
        JoinColumn(
            name = "oppgave_id",
            referencedColumnName = "id",
            nullable = false
        ),
        JoinColumn(
            name = "oppgave_versjon",
            referencedColumnName = "versjon",
            nullable = false
        )
    )
    val metadata: Set<VersjonMetadata> = setOf()
) {

    constructor(
        id: Long,
        versjon: Int,
        journalpostId: String? = null,
        saksreferanse: String? = null,
        mappeId: Long? = null,
        status: Status,
        tildeltEnhetsnr: String,
        opprettetAvEnhetsnr: String? = null,
        endretAvEnhetsnr: String? = null,
        tema: String,
        temagruppe: String? = null,
        behandlingstema: String? = null,
        oppgavetype: String,
        behandlingstype: String? = null,
        prioritet: Prioritet,
        tilordnetRessurs: String? = null,
        beskrivelse: String? = null,
        fristFerdigstillelse: LocalDate,
        aktivDato: LocalDate,
        opprettetAv: String,
        endretAv: String? = null,
        opprettetTidspunkt: LocalDateTime,
        endretTidspunkt: LocalDateTime? = null,
        ferdigstiltTidspunkt: LocalDateTime? = null,
        behandlesAvApplikasjon: String? = null,
        journalpostkilde: String? = null,
        ident: VersjonIdent? = null,
        metadata: Map<MetadataNoekkel, String>
    ) : this(
        id = id,
        versjon = versjon,
        journalpostId = journalpostId,
        saksreferanse = saksreferanse,
        mappeId = mappeId,
        status = status,
        tildeltEnhetsnr = tildeltEnhetsnr,
        opprettetAvEnhetsnr = opprettetAvEnhetsnr,
        endretAvEnhetsnr = endretAvEnhetsnr,
        tema = tema,
        temagruppe = temagruppe,
        behandlingstema = behandlingstema,
        oppgavetype = oppgavetype,
        behandlingstype = behandlingstype,
        prioritet = prioritet,
        tilordnetRessurs = tilordnetRessurs,
        beskrivelse = beskrivelse,
        fristFerdigstillelse = fristFerdigstillelse,
        aktivDato = aktivDato,
        opprettetAv = opprettetAv,
        endretAv = endretAv,
        opprettetTidspunkt = opprettetTidspunkt,
        endretTidspunkt = endretTidspunkt,
        ferdigstiltTidspunkt = ferdigstiltTidspunkt,
        behandlesAvApplikasjon = behandlesAvApplikasjon,
        journalpostkilde = journalpostkilde,
        ident = ident,
        metadata = metadata.map { VersjonMetadata(noekkel = it.key, verdi = it.value) }.toSet()
    )

    fun statuskategori(): Statuskategori = status.kategoriForStatus()

    fun metadataAsMap(): Map<MetadataNoekkel, String> {
        return metadata.map { it.noekkel to it.verdi }.toMap()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OppgaveKopiVersjon

        if (id != other.id) return false
        if (versjon != other.versjon) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + versjon
        return result
    }

    override fun toString(): String {
        return "OppgaveKopiVersjon(id=$id, versjon=$versjon, journalpostId=$journalpostId, saksreferanse=$saksreferanse, mappeId=$mappeId, status=$status, tildeltEnhetsnr='$tildeltEnhetsnr', opprettetAvEnhetsnr=$opprettetAvEnhetsnr, endretAvEnhetsnr=$endretAvEnhetsnr, tema='$tema', temagruppe=$temagruppe, behandlingstema=$behandlingstema, oppgavetype='$oppgavetype', behandlingstype=$behandlingstype, prioritet=$prioritet, tilordnetRessurs=$tilordnetRessurs, beskrivelse=$beskrivelse, fristFerdigstillelse=$fristFerdigstillelse, aktivDato=$aktivDato, opprettetAv='$opprettetAv', endretAv=$endretAv, opprettetTidspunkt=$opprettetTidspunkt, endretTidspunkt=$endretTidspunkt, ferdigstiltTidspunkt=$ferdigstiltTidspunkt, behandlesAvApplikasjon=$behandlesAvApplikasjon, journalpostkilde=$journalpostkilde, ident=$ident, metadata=$metadata)"
    }

}