package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "utfall", schema = "kodeverk")
class Utfall(
    @Id
    val id: Int,
    @Column(name = "navn")
    val navn: String,
    @Column(name = "beskrivelse")
    val beskrivelse: String?
) {

    override fun toString(): String {
        return "Utfall(id=$id, " +
                "navn=$navn)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Utfall

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
