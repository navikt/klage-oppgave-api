package no.nav.klage.oppgave.domain.klage

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "part_id", schema = "klage")
class PartId(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    var type: PartIdType,
    @Column(name = "value")
    val value: String,
) {
    fun copy() = PartId(type = this.type, value = this.value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PartId

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

enum class PartIdType {
    PERSON, VIRKSOMHET
}
