package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.api.view.BehandlingSkyggeView
import no.nav.klage.oppgave.api.view.EndringView
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.TemaConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "endring", schema = "klage")
class Endring(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "saksbehandler")
    val saksbehandler: String,
    @Column(name = "type")
    val type: Endringstype,
    @Column(name = "melding")
    val melding: String,
    @Column(name = "dato_lest")
    val datoLest: LocalDateTime? = null,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "behandling_skygge_id", nullable = false)
    val behandlingSkygge: BehandlingSkygge,
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now()
) {
    fun toEndringView() = EndringView(
        id = this.id,
        saksbehandler = this.saksbehandler,
        type = this.type,
        melding = this.melding,
        created = this.created,
        behandlingSkygge = BehandlingSkyggeView(
            id = this.behandlingSkygge.id,
            hjemmel = this.behandlingSkygge.hjemmel,
            frist = this.behandlingSkygge.frist,
            tema = this.behandlingSkygge.tema
        )
    )
}

@Entity
@Table(name = "behandling_skygge", schema = "klage")
class BehandlingSkygge(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "hjemmel")
    val hjemmel: String,
    @Column(name = "frist")
    val frist: LocalDate?,
    @Column(name = "tema_id")
    @Convert(converter = TemaConverter::class)
    val tema: Tema
)

enum class Endringstype {
    FEIL, VARSEL
}
