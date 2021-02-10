package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.*
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "kvalitetsvurdering", schema = "klage")
class Kvalitetsvurdering(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "grunn_id")
    @Convert(converter = GrunnConverter::class)
    var grunn: Grunn? = null,
    @Column(name = "eoes_id")
    @Convert(converter = EoesConverter::class)
    val eoes: Eoes? = null,
    @Column(name = "raadfoert_med_lege_id")
    @Convert(converter = RaadfoertMedLegeConverter::class)
    val raadfoertMedLege: RaadfoertMedLege? = null,
    @Column(name = "intern_vurdering")
    val internVurdering: String? = null,
    @Column(name = "send_tilbakemelding")
    val sendTilbakemelding: Boolean? = null,
    @Column(name = "tilbakemelding")
    val tilbakemelding: String? = null,
    @Column(name = "mottaker_saksbehandlerident")
    val mottakerSaksbehandlerident: String? = null,
    @Column(name = "mottaker_enhet")
    val mottakerEnhet: String? = null,
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "modified")
    val modified: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        return "Tilbakemelding(id=$id, " +
                "modified=$modified, " +
                "created=$created)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Kvalitetsvurdering

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
