package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.Hjemmel
import no.nav.klage.oppgave.domain.kodeverk.HjemmelConverter
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import no.nav.klage.oppgave.domain.kodeverk.UtfallConverter
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "vedtak", schema = "klage")
class Vedtak(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "utfall_id")
    @Convert(converter = UtfallConverter::class)
    var utfall: Utfall? = null,
    @ElementCollection(targetClass = Hjemmel::class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "vedtak_hjemmel",
        schema = "klage",
        joinColumns = [JoinColumn(name = "vedtak_id", referencedColumnName = "id", nullable = false)]
    )
    @Convert(converter = HjemmelConverter::class)
    @Column(name = "id")
    var hjemler: MutableSet<Hjemmel> = mutableSetOf(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinTable(
        name = "brevmottaker",
        schema = "klage",
        joinColumns = [JoinColumn(name = "vedtak_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "mottaker_part_id", referencedColumnName = "id")]
    )
    var brevmottakere: MutableSet<PartId> = mutableSetOf(),
    @Column(name = "modified")
    var modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "journalpost_id")
    var journalpostId: String? = null,
    @Column(name = "finalized")
    var finalized: LocalDateTime? = null
) {
    override fun toString(): String {
        return "Vedtak(id=$id, " +
                "modified=$modified, " +
                "created=$created)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vedtak

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
