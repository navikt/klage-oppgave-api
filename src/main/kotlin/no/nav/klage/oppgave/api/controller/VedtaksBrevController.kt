package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.domain.vedtaksbrev.BrevElementView
import no.nav.klage.oppgave.domain.vedtaksbrev.VedtaksBrevView
import no.nav.klage.oppgave.service.VedtaksBrevService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RequestMapping("/vedtaksbrev")
class VedtaksBrevController(
    private val vedtaksBrevService: VedtaksBrevService
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Opprett vedtaksbrev",
        notes = "Oppretter vedtaksbrev. Input må inneholde id på klagebehandling og spesifisere mal."
    )
    @PostMapping
    fun createVedtaksBrev(
        @RequestBody vedtaksBrevView: VedtaksBrevView
    ): VedtaksBrevView {
        return vedtaksBrevService.createVedtaksBrev(vedtaksBrevView)
    }

    @ApiOperation(
        value = "Hent vedtaksbrev",
        notes = "Henter opprettet vedtaksbrev. Dersom klagebehandling oppgis hentes brev knyttet til denne, dersom denne er utelatt og brevId oppgis hentes det spesifikke brevet."
    )
    @GetMapping
    fun getVedtaksBrev(
        @ApiParam(value = "ID på klagebehandling")
        @RequestParam klagebehandlingId: UUID?,
        @ApiParam(value = "ID på vedtaksbrev")
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

    @ApiOperation(
        value = "Slett vedtaksbrev",
        notes = "Sletter vedtaksbrev."
    )
    @DeleteMapping("/{brevId}")
    fun deleteBrev(
        @ApiParam(value = "ID på vedtaksbrev.")
        @PathVariable brevId: UUID
    ) {
        return vedtaksBrevService.deleteVedtaksbrev(brevId)
    }

    @ApiOperation(
        value = "Oppdater vedtaksbrev",
        notes = "Oppdaterer vedtaksbrev."
    )
    @PutMapping("/{brevId}/element")
    fun updateElement(
        @ApiParam(value = "ID på vedtaksbrev.")
        @PathVariable brevId: UUID,
        @ApiParam(value = "Element i brevet som skal oppdateres.")
        @RequestBody element: BrevElementView
    ): BrevElementView? {
        return vedtaksBrevService.updateBrevElement(brevId, element)
    }
}