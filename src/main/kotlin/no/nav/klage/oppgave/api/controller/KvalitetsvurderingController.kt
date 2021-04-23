package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.AuditLogger
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
class KvalitetsvurderingController(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val klagebehandlingService: KlagebehandlingService,
    private val klagebehandlingMapper: KlagebehandlingMapper,
    private val auditLogger: AuditLogger
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/klagebehandlinger/{id}/kvalitetsvurdering")
    fun getKvalitetsvurdering(
        @PathVariable("id") klagebehandlingId: String
    ): KvalitetsvurderingView {
        logger.debug(
            "getKvalitetsvurdering is requested by ident {} for klagebehandlingId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.getKlagebehandling(klagebehandlingId.toUUIDOrException())
        )
    }

    @PutMapping("/klagebehandlinger/{id}/kvalitetsvurdering/grunn")
    fun putKvalitetsvurderingGrunn(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingGrunnInput
    ): KvalitetsvurderingView {
        logger.debug(
            "putKvalitetsvurderingGrunn is requested by ident {} for klagebehandlingId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingGrunn(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.grunn,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/kvalitetsvurdering/eoes")
    fun putKvalitetsvurderingEoes(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingEoesInput
    ): KvalitetsvurderingView {
        logger.debug(
            "putKvalitetsvurderingEoes is requested by ident {} for klagebehandlingId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingEoes(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.eoes,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/kvalitetsvurdering/raadfoertmedlege")
    fun putKvalitetsvurderingRaadfoertMedLege(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingRaadfoertMedLegeInput
    ): KvalitetsvurderingView {
        logger.debug(
            "putKvalitetsvurderingRaadfoertMedLege is requested by ident {} for klagebehandlingId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingRaadfoertMedLege(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.raadfoertMedLege,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/kvalitetsvurdering/internvurdering")
    fun putKvalitetsvurderingInternVurdering(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingInternVurderingInput
    ): KvalitetsvurderingView {
        logger.debug(
            "putKvalitetsvurderingInternVurdering is requested by ident {} for klagebehandlingId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingInternVurdering(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.internVurdering,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/kvalitetsvurdering/sendtilbakemelding")
    fun putKvalitetsvurderingSendTilbakemelding(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingSendTilbakemeldingInput
    ): KvalitetsvurderingView {
        logger.debug(
            "putKvalitetsvurderingSendTilbakemelding is requested by ident {} for klagebehandlingId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingSendTilbakemelding(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.sendTilbakemelding,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/kvalitetsvurdering/tilbakemelding")
    fun putKvalitetsvurderingTilbakemelding(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingTilbakemeldingInput
    ): KvalitetsvurderingView {
        logger.debug(
            "putKvalitetsvurderingTilbakemelding is requested by ident {} for klagebehandlingId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingTilbakemelding(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.tilbakemelding,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    private fun String.toUUIDOrException() =
        try {
            UUID.fromString(this)
        } catch (e: Exception) {
            logger.error("KlagebehandlingId could not be parsed as an UUID", e)
            throw BehandlingsidWrongFormatException("KlagebehandlingId could not be parsed as an UUID")
        }
}
