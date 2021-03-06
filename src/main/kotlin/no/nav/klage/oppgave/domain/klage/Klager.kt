package no.nav.klage.oppgave.domain.klage

import javax.persistence.*

@Embeddable
data class Klager(
    @Embedded
    @AttributeOverrides(
        value = [
            AttributeOverride(name = "type", column = Column(name = "klager_type")),
            AttributeOverride(name = "value", column = Column(name = "klager_value"))
        ]
    )
    val partId: PartId,
    @Embedded
    @AttributeOverrides(
        value = [
            AttributeOverride(name = "partId.type", column = Column(name = "klager_prosessfullmektig_type")),
            AttributeOverride(name = "partId.value", column = Column(name = "klager_prosessfullmektig_value")),
            AttributeOverride(name = "skalPartenMottaKopi", column = Column(name = "klager_skal_motta_kopi"))

        ]
    )
    val prosessfullmektig: Prosessfullmektig? = null
)
