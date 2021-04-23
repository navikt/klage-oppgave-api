package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.AuditLogEvent
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
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingDetaljerController(
    private val klagebehandlingService: KlagebehandlingService,
    private val klagebehandlingMapper: KlagebehandlingMapper,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val auditLogger: AuditLogger
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/klagebehandlinger/{id}/detaljer")
    fun getKlagebehandlingDetaljer(
        @PathVariable("id") klagebehandlingId: String
    ): KlagebehandlingDetaljerView {
        logMethodDetails("getKlagebehandlingDetaljer", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.getKlagebehandling(klagebehandlingId.toUUIDOrException())
        ).also {
            auditLogger.log(
                AuditLogEvent(
                    navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent(),
                    personFnr = it.foedselsnummer
                )
            )
        }
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/sakstype")
    fun putSakstype(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KlagebehandlingTypeInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putSakstype", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setSakstype(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.type,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/tema")
    fun putTema(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KlagebehandlingTemaInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putTema", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setTema(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.tema,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/innsendt")
    fun putInnsendt(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KlagebehandlingInnsendtInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putInnsendt", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setInnsendt(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.innsendt,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/mottattfoersteinstans")
    fun putMottattFoersteinstans(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KlagebehandlingMottattFoersteinstansInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putMottattFoersteinstans", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setMottattFoersteinstans(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.mottattFoersteinstans,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/mottattklageinstans")
    fun putMottattKlageinstans(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KlagebehandlingMottattKlageinstansInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putMottattKlageinstans", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setMottattKlageinstans(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.mottattKlageinstans,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/frist")
    fun putFrist(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KlagebehandlingFristInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putFrist", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setFrist(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.frist,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/avsendersaksbehandlerident")
    fun putAvsenderSaksbehandlerident(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KlagebehandlingAvsenderSaksbehandleridentFoersteinstansInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putAvsenderSaksbehandlerident", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setAvsenderSaksbehandleridentFoersteinstans(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.avsenderSaksbehandlerident,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/avsenderenhet")
    fun putAvsenderEnhet(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KlagebehandlingAvsenderEnhetFoersteinstansInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putAvsenderEnhet", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setAvsenderEnhetFoersteinstans(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.avsenderEnhet,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/medunderskriverident")
    fun putMedunderskriverident(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KlagebehandlingMedunderskriveridentInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putMedunderskriverident", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setMedunderskriverident(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.medunderskriverident,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/grunn")
    fun putKvalitetsvurderingGrunn(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingGrunnInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putKvalitetsvurderingGrunn", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setKvalitetsvurderingGrunn(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.grunn,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/eoes")
    fun putKvalitetsvurderingEoes(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingEoesInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putKvalitetsvurderingEoes", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setKvalitetsvurderingEoes(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.eoes,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/raadfoertmedlege")
    fun putKvalitetsvurderingRaadfoertMedLege(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingRaadfoertMedLegeInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putKvalitetsvurderingRaadfoertMedLege", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setKvalitetsvurderingRaadfoertMedLege(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.raadfoertMedLege,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/internvurdering")
    fun putKvalitetsvurderingInternVurdering(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingInternVurderingInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putKvalitetsvurderingInternVurdering", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setKvalitetsvurderingInternVurdering(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.internVurdering,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/sendtilbakemelding")
    fun putKvalitetsvurderingSendTilbakemelding(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingSendTilbakemeldingInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putKvalitetsvurderingSendTilbakemelding", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.setKvalitetsvurderingSendTilbakemelding(
                klagebehandlingId.toUUIDOrException(),
                input.klagebehandlingVersjon,
                input.sendTilbakemelding,
                innloggetSaksbehandlerRepository.getInnloggetIdent()
            )
        )
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/tilbakemelding")
    fun putKvalitetsvurderingTilbakemelding(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingTilbakemeldingInput
    ): KlagebehandlingDetaljerView {
        logMethodDetails("putKvalitetsvurderingTilbakemelding", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
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

    private fun logMethodDetails(methodName: String, klagebehandlingId: String) {
        logger.debug(
            "{} is requested by ident {} for klagebehandlingId {}",
            methodName,
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
    }
}
