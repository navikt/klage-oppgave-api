package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.api.view.EndringView
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now()
) {
    fun toEndringView() = EndringView(
        id = this.id,
        saksbehandler = this.saksbehandler,
        type = this.type,
        melding = this.melding,
        created = this.created
    )
}

enum class Endringstype {
    FEIL, VARSEL
}
