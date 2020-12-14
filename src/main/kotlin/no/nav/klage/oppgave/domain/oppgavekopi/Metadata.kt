package no.nav.klage.oppgave.domain.oppgavekopi

import javax.persistence.*

@Entity
@Table(name = "metadata", schema = "oppgave")
class Metadata(
    @Id
    @Column(name = "id")
    @SequenceGenerator(
        name = "metadata_seq",
        sequenceName = "oppgave.metadata_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metadata_seq")
    val id: Long? = null,
    @Column(name = "nokkel")
    val noekkel: MetadataNoekkel,
    @Column(name = "verdi")
    val verdi: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Metadata

        if (noekkel != other.noekkel) return false
        if (verdi != other.verdi) return false

        return true
    }

    override fun hashCode(): Int {
        var result = noekkel.hashCode()
        result = 31 * result + verdi.hashCode()
        return result
    }

    override fun toString(): String {
        return "Metadata(id=$id, noekkel=$noekkel, verdi='$verdi')"
    }
}