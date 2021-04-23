package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.DokumentKnytning
import no.nav.klage.oppgave.api.view.DokumentReferanserResponse
import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.DokumentService
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.data.repository.query.Param
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Tag(name = "kabal-api")
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

    @Operation(
        summary = "Hent dokumenter for en klagebehandling",
        description = "Henter alle dokumenter om en person som saksbehandler har tilgang til."
    )
    @GetMapping("/klagebehandlinger/{behandlingsid}/alledokumenter", produces = ["application/json"])
    fun fetchDokumenter(
        @Param(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String,
        @RequestParam(required = false, name = "antall", defaultValue = "10") pageSize: Int,
        @RequestParam(required = false, name = "forrigeSide") previousPageRef: String? = null
    ): DokumenterResponse {
        val klagebehandlingId = parseAndValidate(behandlingsid)
        return klagebehandlingService.fetchDokumentlisteForKlagebehandling(klagebehandlingId, pageSize, previousPageRef)
    }

    @Operation(
        summary = "Hent dokumenter knyttet til en klagebehandling",
        description = "Henter dokumentene som saksbehandler har markert at skal knyttes til klagebehandlingen."
    )
    @GetMapping("/klagebehandlinger/{behandlingsid}/dokumenter", produces = ["application/json"])
    fun fetchConnectedDokumenter(
        @Param(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String
    ): DokumenterResponse {
        val klagebehandlingId = parseAndValidate(behandlingsid)
        return klagebehandlingService.fetchJournalposterConnectedToKlagebehandling(klagebehandlingId)
    }

    @Operation(
        summary = "Hent IDene til dokumentene knyttet til en klagebehandling",
        description = "Henter IDene til dokumentene som saksbehandler har markert at skal knyttes til klagebehandlingen."
    )
    @GetMapping("/klagebehandlinger/{behandlingsid}/dokumentreferanser", produces = ["application/json"])
    fun fetchConnectedDokumentIder(
        @Param(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String
    ): DokumentReferanserResponse {
        val klagebehandlingId = parseAndValidate(behandlingsid)
        return DokumentReferanserResponse(
            klagebehandlingService.fetchJournalpostIderConnectedToKlagebehandling(
                klagebehandlingId
            )
        )
    }

    @Operation(
        summary = "Fjerner et dokument fra en klagebehandling",
        description = "Sletter knytningen mellom en journalpost fra SAF og klagebehandlingen den har vært knyttet til."
    )
    @DeleteMapping(
        "/klagebehandlinger/{behandlingsId}/journalposter/{journalpostId}/dokumenter/{dokumentInfoId}",
        produces = ["application/json"]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun disconnectDokument(
        @Param(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsId: String,
        @PathVariable journalpostId: String,
        @PathVariable dokumentInfoId: String
    ) {
        val klagebehandlingId = parseAndValidate(behandlingsId)
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        klagebehandlingService.disconnectDokumentFromKlagebehandling(
            klagebehandlingId,
            null, //dropper optimistic locking her
            journalpostId,
            dokumentInfoId,
            innloggetIdent
        )
    }

    @Operation(
        summary = "Knytter et dokument til en klagebehandling",
        description = "Knytter en journalpost fra SAF til klagebehandlingen."
    )
    @PostMapping("/klagebehandlinger/{behandlingsid}/dokumenter", produces = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun connectDokument(
        @Param(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String,
        @RequestBody dokumentKnytning: DokumentKnytning
    ) {
        val klagebehandlingId = parseAndValidate(behandlingsid)
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        klagebehandlingService.connectDokumentToKlagebehandling(
            klagebehandlingId,
            null, //dropper optimistic locking her
            dokumentKnytning.journalpostId,
            dokumentKnytning.dokumentInfoId,
            innloggetIdent
        )
    }

    @ResponseBody
    @GetMapping("/klagebehandlinger/{behandlingsId}/journalposter/{journalpostId}/dokumenter/{dokumentInfoId}")
    fun getArkivertDokument(
        @Param(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsId: String,
        @Param(value = "Id til journalpost")
        @PathVariable journalpostId: String,
        @Param(value = "Id til dokumentInfo")
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
