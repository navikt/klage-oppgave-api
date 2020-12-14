package no.nav.klage.oppgave.domain.oppgavekopi

import javax.persistence.*

@Entity
@Table(name = "versjonmetadata", schema = "oppgave")
class VersjonMetadata(
    @Id
    @Column(name = "id")
    @SequenceGenerator(
        name = "versjonmetadata_seq",
        sequenceName = "oppgave.versjonmetadata_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "versjonmetadata_seq")
    var id: Long? = null,
    @Column(name = "nokkel")
    var noekkel: MetadataNoekkel,
    @Column(name = "verdi")
    var verdi: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VersjonMetadata

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
        return "VersjonMetadata(id=$id, noekkel=$noekkel, verdi='$verdi')"
    }
}