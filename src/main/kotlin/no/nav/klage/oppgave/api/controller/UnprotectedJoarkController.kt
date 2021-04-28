package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.service.DokumentService
import no.nav.klage.oppgave.service.VedleggService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Profile("dev-gcp")
@RestController
@Unprotected
@RequestMapping("testjournalfoering")
class UnprotectedJoarkController(
    private val vedleggService: VedleggService,
    private val dokumentService: DokumentService
) {

    @PostMapping(value = ["/klagebehandlinger/{id}/journalfoer"], consumes = ["multipart/form-data"])
    fun journalfoer(
        @PathVariable id: String,
        @RequestParam vedlegg: MultipartFile? = null,
        @RequestParam finalize: Boolean,
        @RequestParam fagsak: Boolean
    ): String {
        return vedleggService.addVedleggSystemUser(id.toUUIDOrException(), vedlegg, finalize, fagsak)
    }

    @PostMapping(value = ["/klagebehandlinger/{id}/journalfoer/{journalpostId}"], consumes = ["multipart/form-data"])
    fun oppdater(
        @PathVariable id: String,
        @PathVariable journalpostId: String,
        @RequestParam vedlegg: MultipartFile,
    ): String {
        return vedleggService.updateVedleggSystemUser(id.toUUIDOrException(), vedlegg, journalpostId)
    }

    @PatchMapping("/klagebehandlinger/{journalpostId}/ferdigstill")
    fun ferdigstill(
        @PathVariable journalpostId: String
    ): String {
        return vedleggService.ferdigstillJournalpost(journalpostId)
//        return joarkClient.createJournalpost(klagebehandlingService.getKlagebehandling(id.toUUIDOrException()), null, true)
    }

    @PatchMapping("/klagebehandlinger/{journalpostId}/avbryt")
    fun avbryt(
        @PathVariable journalpostId: String
    ): String {
        return vedleggService.avbrytJournalpost(journalpostId)
//        return joarkClient.createJournalpost(klagebehandlingService.getKlagebehandling(id.toUUIDOrException()), null, true)
    }

    @GetMapping("/klagebehandlinger/{journalpostId}/pdf")
    fun getInnsendtKlage(
        @PathVariable journalpostId: String
    ): ResponseEntity<ByteArray> {
        val content = dokumentService.getMainDokument(journalpostId).bytes
        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.valueOf("application/pdf")
        responseHeaders.add("Content-Disposition", "inline; filename=" + "vedlegg.pdf")
        return ResponseEntity(
            content,
            responseHeaders,
            HttpStatus.OK
        )
    }

    private fun String.toUUIDOrException() =
        try {
            UUID.fromString(this)
        } catch (e: Exception) {
            throw BehandlingsidWrongFormatException("KlagebehandlingId could not be parsed as an UUID")
        }
}