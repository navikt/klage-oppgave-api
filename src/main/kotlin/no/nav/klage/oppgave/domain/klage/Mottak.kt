package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "mottak", schema = "klage")
class Mottak(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Version
    @Column(name = "versjon")
    val versjon: Long = 1L,
    @Column(name = "tema_id")
    @Convert(converter = TemaConverter::class)
    var tema: Tema,
    @Column(name = "sakstype_id")
    @Convert(converter = SakstypeConverter::class)
    var sakstype: Sakstype,
    @Column(name = "referanse_id")
    var referanseId: String? = null,
    @Column(name = "innsyn_url")
    val innsynUrl: String? = null,
    @Column(name = "foedselsnummer")
    var foedselsnummer: String? = null,
    @Column(name = "organisasjonsnummer")
    var organisasjonsnummer: String? = null,
    @Column(name = "virksomhetsnummer")
    val virksomhetsnummer: String? = null,
    @Column(name = "hjemmel_liste")
    var hjemmelListe: String? = null,
    @Column(name = "beskrivelse")
    var beskrivelse: String? = null,
    @Column(name = "avsender_saksbehandlerident")
    var avsenderSaksbehandlerident: String? = null,
    @Column(name = "avsender_enhet")
    var avsenderEnhet: String? = null,
    @Column(name = "oversendt_klageinstans_enhet")
    var oversendtKaEnhet: String? = null,
    @Column(name = "oversendelsesbrev_journalpost_id")
    var oversendelsesbrevJournalpostId: String? = null,
    @Column(name = "brukers_klage_journalpost_id")
    var brukersKlageJournalpostId: String? = null,
    @Column(name = "dato_innsendt")
    val innsendtDato: LocalDate? = null,
    @Column(name = "dato_mottatt_foersteinstans")
    val mottattNavDato: LocalDate? = null,
    @Column(name = "dato_oversendt_klageinstans")
    var oversendtKaDato: LocalDate,
    @Column(name = "dato_frist_fra_foersteinstans")
    var fristFraFoersteinstans: LocalDate? = null,
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "modified")
    val modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "kilde")
    @Enumerated(EnumType.STRING)
    val kilde: Kilde,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    val mottakAdresser: MutableSet<MottakAdresse> = mutableSetOf(),
) {

    fun hjemler(): List<String> = hjemmelListe?.split(",") ?: emptyList()

    override fun toString(): String {
        return "Mottak(id=$id, " +
                "created=$created)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Mottak

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
