package no.nav.klage.oppgave.domain.vedtaksbrev

import no.nav.klage.oppgave.domain.vedtaksbrev.enums.VedtaksBrevMal
import no.nav.klage.oppgave.service.VedtaksBrevService
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "vedtaksbrev", schema = "klage")
data class VedtaksBrev(
    @Id
    val id: UUID = UUID.randomUUID(),

    val klagebehandlingId: UUID,

    @Column(name = "brev_mal")
    @Enumerated(EnumType.STRING)
    val brevMal: VedtaksBrevMal,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "brev_id", referencedColumnName = "id", nullable = false)
    var brevElements: List<BrevElement>? = null
)

fun VedtaksBrev.toVedtaksBrevView(): VedtaksBrevView {
    return VedtaksBrevView(
        id = id,
        klagebehandlingId = klagebehandlingId,
        brevMal = brevMal,
        elements = brevElements
            ?.map { it.toBrevElementView() }
            ?.sortedWith(VedtaksBrevService.BrevElementComparator(brevMal.elementOrder))
    )
}