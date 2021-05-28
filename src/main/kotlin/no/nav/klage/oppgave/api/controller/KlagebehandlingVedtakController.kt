package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.AuditLogEvent
import no.nav.klage.oppgave.domain.kodeverk.Grunn
import no.nav.klage.oppgave.domain.kodeverk.Hjemmel
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.DokumentService
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.VedtakService
import no.nav.klage.oppgave.util.AuditLogger
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingVedtakController(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val klagebehandlingMapper: KlagebehandlingMapper,
    private val vedtakService: VedtakService,
    private val auditLogger: AuditLogger,
    private val klagebehandlingService: KlagebehandlingService,
    private val dokumentService: DokumentService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/klagebehandlinger/{klagebehandlingid}/vedtak/{vedtakid}")
    fun getVedtak(
        @PathVariable("klagebehandlingid") klagebehandlingId: String,
        @PathVariable("vedtakid") vedtakId: String
    ): VedtakView {
        logMethodDetails("getVedtak", klagebehandlingId, vedtakId)
        val klagebehandling = klagebehandlingService.getKlagebehandling(klagebehandlingId.toUUIDOrException())
            .also {
                auditLogger.log(
                    AuditLogEvent(
                        navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent(),
                        personFnr = it.klager.partId.value
                    )
                )
            }
        return klagebehandlingMapper.mapVedtakToVedtakView(
            vedtakService.getVedtak(
                klagebehandling,
                vedtakId.toUUIDOrException()
            ),
            vedtakService.getVedleggView(
                klagebehandling,
                vedtakId.toUUIDOrException(),
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{klagebehandlingid}/vedtak/{vedtakid}/utfall")
    fun putUtfall(
        @PathVariable("klagebehandlingid") klagebehandlingId: String,
        @PathVariable("vedtakid") vedtakId: String,
        @RequestBody input: VedtakUtfallInput
    ): VedtakView {
        logMethodDetails("putUtfall", klagebehandlingId, vedtakId)
        return klagebehandlingMapper.mapVedtakToVedtakView(
            vedtakService.oppdaterUtfall(
                klagebehandlingId.toUUIDOrException(),
                vedtakId.toUUIDOrException(),
                input,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{klagebehandlingid}/vedtak/{vedtakid}/grunn")
    fun putGrunn(
        @PathVariable("klagebehandlingid") klagebehandlingId: String,
        @PathVariable("vedtakid") vedtakId: String,
        @RequestBody input: VedtakGrunnInput
    ): VedtakView {
        logMethodDetails("putGrunn", klagebehandlingId, vedtakId)
        return klagebehandlingMapper.mapVedtakToVedtakView(
            vedtakService.oppdaterGrunn(
                klagebehandlingId.toUUIDOrException(),
                vedtakId.toUUIDOrException(),
                input,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{klagebehandlingid}/vedtak/{vedtakid}/hjemler")
    fun putHjemler(
        @PathVariable("klagebehandlingid") klagebehandlingId: String,
        @PathVariable("vedtakid") vedtakId: String,
        @RequestBody input: VedtakHjemlerInput
    ): VedtakView {
        logMethodDetails("putHjemler", klagebehandlingId, vedtakId)
        return klagebehandlingMapper.mapVedtakToVedtakView(
            vedtakService.oppdaterHjemler(
                klagebehandlingId.toUUIDOrException(),
                vedtakId.toUUIDOrException(),
                input,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PostMapping("/klagebehandlinger/{klagebehandlingid}/vedtak/{vedtakid}/vedlegg")
    fun postVedlegg(
        @PathVariable("klagebehandlingid") klagebehandlingId: String,
        @PathVariable("vedtakid") vedtakId: String,
        @ModelAttribute input: VedtakVedleggInput
    ): VedleggView? {
        logMethodDetails("postVedlegg", klagebehandlingId, vedtakId)

        return vedtakService.knyttVedtaksFilTilVedtak(
            klagebehandlingId.toUUIDOrException(),
            vedtakId.toUUIDOrException(),
            input,
            innloggetSaksbehandlerRepository.getInnloggetIdent()
        )
    }

    @PostMapping("/klagebehandlinger/{klagebehandlingid}/vedtak/{vedtakid}/fullfoer")
    fun fullfoerVedtak(
        @PathVariable("klagebehandlingid") klagebehandlingId: String,
        @PathVariable("vedtakid") vedtakId: String,
        @RequestBody input: VedtakFullfoerInput
    ) {
        logMethodDetails("fullfoerVedtak", klagebehandlingId, vedtakId)
        vedtakService.ferdigstillVedtak(
            klagebehandlingId.toUUIDOrException(),
            vedtakId.toUUIDOrException(),
            input,
            innloggetSaksbehandlerRepository.getInnloggetIdent()
        )
    }

    @ResponseBody
    @GetMapping("/klagebehandlinger/{klagebehandlingid}/vedtak/{vedtakid}/pdf")
    fun getVedlegg(
        @PathVariable("klagebehandlingid") klagebehandlingId: String,
        @PathVariable("vedtakid") vedtakId: String,
    ): ResponseEntity<ByteArray> {
        logMethodDetails("getVedlegg", klagebehandlingId, vedtakId)
        val klagebehandling = klagebehandlingService.getKlagebehandling(klagebehandlingId.toUUIDOrException())
        val arkivertDokument = vedtakService.getVedleggArkivertDokument(
            klagebehandling,
            vedtakId.toUUIDOrException(),
            innloggetSaksbehandlerRepository.getInnloggetIdent()
        )
        val vedleggView = vedtakService.getVedleggView(
            klagebehandling,
            vedtakId.toUUIDOrException(),
            innloggetSaksbehandlerRepository.getInnloggetIdent()
        )
        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.valueOf("application/pdf")
        responseHeaders.add("Content-Disposition", "inline; filename=${vedleggView?.name}")
        return ResponseEntity(
            arkivertDokument.bytes,
            responseHeaders,
            HttpStatus.OK
        )
    }

    private fun String.toUUIDOrException() =
        try {
            UUID.fromString(this)
        } catch (e: Exception) {
            logger.error("Input could not be parsed as an UUID", e)
            throw BehandlingsidWrongFormatException("Input could not be parsed as an UUID")
        }

    private fun logMethodDetails(methodName: String, klagebehandlingId: String, vedtakId: String) {
        logger.debug(
            "{} is requested by ident {} for klagebehandlingId {} and vedtakId {}",
            methodName,
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId,
            vedtakId
        )
    }
}
