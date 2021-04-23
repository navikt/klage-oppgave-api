package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.view.VedtakUtfallInput
import no.nav.klage.oppgave.api.view.VedtakView
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.AuditLogEvent
import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.VedtakService
import no.nav.klage.oppgave.util.AuditLogger
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
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
    private val klagebehandlingService: KlagebehandlingService
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
            vedtakService.getVedtak(klagebehandling, vedtakId.toUUIDOrException())
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
            vedtakService.setUtfall(
                klagebehandlingService.getKlagebehandlingForUpdate(
                    klagebehandlingId.toUUIDOrException(),
                    input.klagebehandlingVersjon
                ),
                vedtakId.toUUIDOrException(),
                input.utfall,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
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