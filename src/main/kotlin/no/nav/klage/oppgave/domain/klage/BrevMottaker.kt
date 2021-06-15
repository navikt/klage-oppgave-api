package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.Rolle
import no.nav.klage.oppgave.domain.kodeverk.RolleConverter
import org.springframework.data.domain.Persistable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "brevmottaker", schema = "klage")
class BrevMottaker(

    @Id
    @JvmField
    val id: UUID = UUID.randomUUID(),
    @Embedded
    @AttributeOverrides(
        value = [
            AttributeOverride(name = "type", column = Column(name = "mottaker_type")),
            AttributeOverride(name = "value", column = Column(name = "mottaker_value"))
        ]
    )
    val partId: PartId,
    @Column(name = "rolle_id")
    @Convert(converter = RolleConverter::class)
    val rolle: Rolle,
    @Column(name = "journalpost_id")
    var journalpostId: String? = null,
    @Column(name = "dokdist_referanse")
    var dokdistReferanse: UUID? = null
) : Persistable<UUID> {

    override fun getId(): UUID = id

    @Transient
    private var isNew = true

    override fun isNew(): Boolean {
        return isNew
    }

    @PrePersist
    @PostLoad
    fun markNotNew() {
        isNew = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BrevMottaker

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "BrevMottaker(id=$id, partId=$partId, rolle=$rolle, journalpostId=$journalpostId, dokdistReferanse=$dokdistReferanse)"
    }
}