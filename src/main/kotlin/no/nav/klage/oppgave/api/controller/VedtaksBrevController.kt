package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.domain.vedtaksbrev.BrevElementView
import no.nav.klage.oppgave.domain.vedtaksbrev.VedtaksBrevView
import no.nav.klage.oppgave.service.VedtaksBrevService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Unprotected
@RequestMapping("/vedtaksbrev")
class VedtaksBrevController(
    private val vedtaksBrevService: VedtaksBrevService
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping
    fun createVedtaksBrev(
        @RequestBody vedtaksBrevView: VedtaksBrevView
    ): VedtaksBrevView {
        return vedtaksBrevService.createVedtaksBrev(vedtaksBrevView)
    }

    @GetMapping
    fun getVedtaksBrevByKlagebehandlingId(
        @RequestParam klagebehandlingId: UUID?,
        @RequestParam brevId: UUID?
    ): List<VedtaksBrevView> {
        return if (klagebehandlingId != null) {
            vedtaksBrevService.getVedtaksBrevByKlagebehandlingId(klagebehandlingId)
        } else if (brevId != null) {
            listOf(vedtaksBrevService.getVedtaksBrev(brevId))
        } else throw Exception()
    }

    @DeleteMapping("/{brevId}")
    fun deleteBrev(
        @PathVariable brevId: UUID
    ) {
        return vedtaksBrevService.deleteVedtaksbrev(brevId)
    }

    @PutMapping("/{brevId}/element")
    fun updateElement(
        @PathVariable brevId: UUID,
        @RequestBody element: BrevElementView
    ): BrevElementView? {
        return vedtaksBrevService.updateBrevElement(brevId, element)
    }
}