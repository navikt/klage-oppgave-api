package no.nav.klage.oppgave.domain.oppgavekopi

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "versjonident", schema = "oppgave")
class VersjonIdent(
    @Id
    @Column(name = "id")
    @SequenceGenerator(
        name = "versjonident_seq",
        sequenceName = "oppgave.versjonident_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "versjonident_seq")
    var id: Long?,
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    var identType: IdentType,
    @Column(name = "verdi")
    var verdi: String,
    @Column(name = "folkeregisterident")
    var folkeregisterident: String? = null,
    @Column(name = "registrert_dato")
    var registrertDato: LocalDate? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VersjonIdent

        if (identType != other.identType) return false
        if (verdi != other.verdi) return false
        if (folkeregisterident != other.folkeregisterident) return false
        if (registrertDato != other.registrertDato) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identType.hashCode()
        result = 31 * result + verdi.hashCode()
        result = 31 * result + (folkeregisterident?.hashCode() ?: 0)
        result = 31 * result + (registrertDato?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "VersjonIdent(id=$id, identType=$identType, verdi='$verdi', folkeregisterident=$folkeregisterident, registrertDato=$registrertDato)"
    }
}
