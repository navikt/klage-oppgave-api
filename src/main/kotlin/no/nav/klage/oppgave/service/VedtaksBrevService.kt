package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.vedtaksbrev.*
import no.nav.klage.oppgave.domain.vedtaksbrev.enums.BrevElementKey
import no.nav.klage.oppgave.domain.vedtaksbrev.enums.VedtaksBrevMal
import no.nav.klage.oppgave.repositories.BrevElementRepository
import no.nav.klage.oppgave.repositories.VedtaksBrevRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class VedtaksBrevService(
    private val vedtaksBrevRepository: VedtaksBrevRepository,
    private val brevElementRepository: BrevElementRepository
) {
    fun createVedtaksBrev(vedtaksBrevView: VedtaksBrevView): VedtaksBrevView {
        val newVedtaksBrev = VedtaksBrev(
            klagebehandlingId = vedtaksBrevView.klagebehandlingId,
            brevMal = vedtaksBrevView.brevMal
        )
        newVedtaksBrev.brevElements = newVedtaksBrev.brevMal.elementOrder.map {
            it.generateDefaultBrevElement(newVedtaksBrev.id)
        }
        vedtaksBrevRepository.save(newVedtaksBrev)
        return newVedtaksBrev.toVedtaksBrevView()
    }

    fun getVedtaksBrev(brevId: UUID): VedtaksBrevView {
        val brev = vedtaksBrevRepository.getOne(brevId)
        return brev.toVedtaksBrevView()
    }

    fun getVedtaksBrevByKlagebehandlingId(klagebehandlingId: UUID): List<VedtaksBrevView> {
        val brevResults = vedtaksBrevRepository.findByKlagebehandlingId(klagebehandlingId)
        return brevResults.map { it.toVedtaksBrevView() }
    }

    fun deleteVedtaksbrev(brevId: UUID) {
        vedtaksBrevRepository.deleteById(brevId)
    }

    fun updateBrevElement(brevId: UUID, inputElement: BrevElementView): BrevElementView? {
        val existingElement = brevElementRepository.findByBrevIdAndKey(brevId, inputElement.key)
        existingElement.updateFields(inputElement)
        return brevElementRepository.save(
            existingElement
        ).toBrevElementView()
    }

    class BrevElementComparator(elementOrder: List<BrevElementKey>) : Comparator<BrevElementView> {
        private val currentElementOrder = elementOrder
        override fun compare(a: BrevElementView, b: BrevElementView): Int {
            return currentElementOrder.indexOf(a.key) - currentElementOrder.indexOf(b.key)
        }
    }
}