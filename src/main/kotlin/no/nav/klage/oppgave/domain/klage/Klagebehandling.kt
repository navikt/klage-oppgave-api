package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "klagebehandling", schema = "klage")
class Klagebehandling(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "foedselsnummer")
    val foedselsnummer: String? = null,
    @Column(name = "tema_id")
    @Convert(converter = TemaConverter::class)
    val tema: Tema,
    @Column(name = "sakstype_id")
    @Convert(converter = SakstypeConverter::class)
    val sakstype: Sakstype,
    @Column(name = "dato_innsendt")
    val innsendt: LocalDate? = null,
    @Column(name = "dato_mottatt_foersteinstans")
    val mottattFoersteinstans: LocalDate? = null,
    @Column(name = " dato_mottatt_klageinstans")
    val mottattKlageinstans: LocalDate,
    @Column(name = "dato_behandling_startet")
    val startet: LocalDate? = null,
    @Column(name = "dato_behandling_avsluttet")
    val avsluttet: LocalDate? = null,
    @Column(name = "frist")
    val frist: LocalDate? = null,
    @Column(name = "tildelt_saksbehandlerident")
    val tildeltSaksbehandlerident: String? = null,
    @Column(name = "tildelt_enhet")
    val tildeltEnhet: String? = null,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "mottak_id", nullable = true)
    val mottak: Mottak,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "vedtak_id", nullable = true)
    val vedtak: Vedtak? = null,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "kvalitetsvurdering_id", nullable = true)
    var kvalitetsvurdering: Kvalitetsvurdering? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    val hjemler: MutableList<Hjemmel> = mutableListOf(),
    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    val oppgavereferanser: MutableList<Oppgavereferanse> = mutableListOf(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "klagebehandling_id", referencedColumnName = "id", nullable = false)
    val saksdokumenter: MutableList<Saksdokument> = mutableListOf(),
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "modified")
    val modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "kilde")
    @Enumerated(EnumType.STRING)
    val kilde: Kilde
) {
    override fun toString(): String {
        return "Behandling(id=$id, " +
                "modified=$modified, " +
                "created=$created)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Klagebehandling

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun getOrCreateKvalitetsvurdering(): Kvalitetsvurdering {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        return kvalitetsvurdering!!
    }
}
