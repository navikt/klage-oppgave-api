package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.domain.vedtaksbrev.BrevElementView
import no.nav.klage.oppgave.domain.vedtaksbrev.VedtaksBrevView
import no.nav.klage.oppgave.service.VedtaksBrevService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.data.repository.query.Param
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RequestMapping("/vedtaksbrev")
class VedtaksBrevController(
    private val vedtaksBrevService: VedtaksBrevService
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Opprett vedtaksbrev",
        description = "Oppretter vedtaksbrev. Input må inneholde id på klagebehandling og spesifisere mal."
    )
    @PostMapping
    fun createVedtaksBrev(
        @RequestBody vedtaksBrevView: VedtaksBrevView
    ): VedtaksBrevView {
        return vedtaksBrevService.createVedtaksBrev(vedtaksBrevView)
    }

    @Operation(
        summary = "Hent vedtaksbrev",
        description = "Henter opprettet vedtaksbrev. Dersom klagebehandling oppgis hentes brev knyttet til denne, dersom denne er utelatt og brevId oppgis hentes det spesifikke brevet."
    )
    @GetMapping
    fun getVedtaksBrev(
        @Param(value = "ID på klagebehandling")
        @RequestParam klagebehandlingId: UUID?,
        @Param(value = "ID på vedtaksbrev")
        @RequestParam brevId: UUID?
    ): List<VedtaksBrevView> {
        return when {
            klagebehandlingId != null -> {
                vedtaksBrevService.getVedtaksBrevByKlagebehandlingId(klagebehandlingId)
            }
            brevId != null -> {
                listOf(vedtaksBrevService.getVedtaksBrev(brevId))
            }
            else -> throw Exception()
        }
    }

    @Operation(
        summary = "Slett vedtaksbrev",
        description = "Sletter vedtaksbrev."
    )
    @DeleteMapping("/{brevId}")
    fun deleteBrev(
        @Param(value = "ID på vedtaksbrev.")
        @PathVariable brevId: UUID
    ) {
        return vedtaksBrevService.deleteVedtaksbrev(brevId)
    }

    @Operation(
        summary = "Oppdater vedtaksbrev",
        description = "Oppdaterer vedtaksbrev."
    )
    @PutMapping("/{brevId}/element")
    fun updateElement(
        @Param(value = "ID på vedtaksbrev.")
        @PathVariable brevId: UUID,
        @Param(value = "Element i brevet som skal oppdateres.")
        @RequestBody element: BrevElementView
    ): BrevElementView? {
        return vedtaksBrevService.updateBrevElement(brevId, element)
    }
}
