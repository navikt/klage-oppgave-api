package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.DokumentService
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class DokumentController(
    private val klagebehandlingService: KlagebehandlingService,
    private val dokumentService: DokumentService,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent dokumenter for en klagebehandling",
        notes = "Henter alle dokumenter om en person som saksbehandler har tilgang til."
    )
    @GetMapping("/klagebehandlinger/{behandlingsid}/alledokumenter", produces = ["application/json"])
    fun fetchDokumenter(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String,
        @RequestParam(required = false, name = "antall", defaultValue = "10") pageSize: Int,
        @RequestParam(required = false, name = "forrigeSide") previousPageRef: String? = null
    ): DokumenterResponse {
        val klagebehandlingId = parseAndValidate(behandlingsid)
        return klagebehandlingService.fetchDokumentlisteForKlagebehandling(klagebehandlingId, pageSize, previousPageRef)
    }

    @ApiOperation(
        value = "Hent dokumenter knyttet til en klagebehandling",
        notes = "Henter dokumentene som saksbehandler har markert at skal knyttes til klagebehandlingen."
    )
    @GetMapping("/klagebehandlinger/{behandlingsid}/dokumenter", produces = ["application/json"])
    fun fetchConnectedDokumenter(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String
    ): DokumenterResponse {
        val klagebehandlingId = parseAndValidate(behandlingsid)
        return klagebehandlingService.fetchJournalposterConnectedToKlagebehandling(klagebehandlingId)
    }

    @ApiOperation(
        value = "Hent IDene til dokumentene knyttet til en klagebehandling",
        notes = "Henter IDene til dokumentene som saksbehandler har markert at skal knyttes til klagebehandlingen."
    )
    @GetMapping("/klagebehandlinger/{behandlingsid}/dokumentreferanser", produces = ["application/json"])
    fun fetchConnectedDokumentIder(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String
    ): DokumentReferanserResponse {
        val klagebehandlingId = parseAndValidate(behandlingsid)
        val klagebehandling =
            klagebehandlingService.getKlagebehandling(klagebehandlingId)
        return DokumentReferanserResponse(
            klagebehandling.versjon, klagebehandling.saksdokumenter.map { it.journalpostId }
        )
    }

    @ApiOperation(
        value = "Fjerner et dokument fra en klagebehandling",
        notes = "Sletter knytningen mellom en journalpost fra SAF og klagebehandlingen den har vært knyttet til."
    )
    @DeleteMapping(
        "/klagebehandlinger/{behandlingsId}/versjon/{behandlingsVersjon}/journalposter/{journalpostId}/dokumenter/{dokumentInfoId}",
        produces = ["application/json"]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun disconnectDokument(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsId: String,
        @PathVariable behandlingsVersjon: Long,
        @PathVariable journalpostId: String,
        @PathVariable dokumentInfoId: String
    ) {
        val klagebehandlingId = parseAndValidate(behandlingsId)
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        klagebehandlingService.disconnectDokumentFromKlagebehandling(
            klagebehandlingId,
            behandlingsVersjon,
            journalpostId,
            dokumentInfoId,
            innloggetIdent
        )
    }

    @ApiOperation(
        value = "Knytter et dokument til en klagebehandling",
        notes = "Knytter en journalpost fra SAF til klagebehandlingen."
    )
    @PostMapping("/klagebehandlinger/{behandlingsid}/dokumenter", produces = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun connectDokument(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String,
        @RequestBody dokumentKnytning: DokumentKnytning
    ) {
        val klagebehandlingId = parseAndValidate(behandlingsid)
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        klagebehandlingService.connectDokumentToKlagebehandling(
            klagebehandlingId,
            dokumentKnytning.klagebehandlingVersjon,
            dokumentKnytning.journalpostId,
            dokumentKnytning.dokumentInfoId,
            innloggetIdent
        )
    }

    @ApiOperation(
        value = "Toggler et dokument til en klagebehandling",
        notes = "Toggler et dokument fra SAF til klagebehandlingen."
    )
    @PostMapping("/klagebehandlinger/{behandlingsid}/toggledokument", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun toggleDokumentConnected(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String,
        @RequestBody toggleDokument: ToggleDokument
    ): ToggleDokumentResponse {
        val klagebehandlingId = parseAndValidate(behandlingsid)
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        val (bleTilknyttet, klagebehandlingVersjon) = klagebehandlingService.toggleDokumentFromKlagebehandling(
            klagebehandlingId,
            toggleDokument.klagebehandlingVersjon,
            toggleDokument.journalpostId,
            toggleDokument.dokumentInfoId,
            innloggetIdent
        )
        return ToggleDokumentResponse(tilknyttet = bleTilknyttet, klagebehandlingVersjon = klagebehandlingVersjon)
    }

    @ResponseBody
    @GetMapping("/klagebehandlinger/{behandlingsId}/journalposter/{journalpostId}/dokumenter/{dokumentInfoId}")
    fun getArkivertDokument(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsId: String,
        @ApiParam(value = "Id til journalpost")
        @PathVariable journalpostId: String,
        @ApiParam(value = "Id til dokumentInfo")
        @PathVariable dokumentInfoId: String

    ): ResponseEntity<ByteArray> {
        val klagebehandlingId = parseAndValidate(behandlingsId)
        logger.debug(
            "Get getArkivertDokument is requested. behandlingsid: {} - journalpostId: {} - dokumentInfoId: {}",
            klagebehandlingId,
            journalpostId,
            dokumentInfoId
        )

        val arkivertDokument = dokumentService.getArkivertDokument(journalpostId, dokumentInfoId)

        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = arkivertDokument.contentType
        responseHeaders.add("Content-Disposition", "inline")
        return ResponseEntity(
            arkivertDokument.bytes,
            responseHeaders,
            HttpStatus.OK
        )
    }

    private fun parseAndValidate(behandlingsid: String): UUID =
        try {
            UUID.fromString(behandlingsid)
        } catch (e: Exception) {
            logger.warn("Unable to parse uuid from $behandlingsid", e)
            throw BehandlingsidWrongFormatException("$behandlingsid is not a valid behandlingsid")
        }
}