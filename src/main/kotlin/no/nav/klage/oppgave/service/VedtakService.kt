package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.joark.JoarkClient
import no.nav.klage.oppgave.clients.saf.graphql.Journalstatus.FERDIGSTILT
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setGrunnInVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setHjemlerInVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setJournalpostIdOgOpplastetInVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setUtfallInVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setVedtakFerdigstiltIJoark
import no.nav.klage.oppgave.domain.klage.Vedtak
import no.nav.klage.oppgave.domain.kodeverk.Grunn
import no.nav.klage.oppgave.domain.kodeverk.Hjemmel
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import no.nav.klage.oppgave.exceptions.JournalpostFinalizationException
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.exceptions.UtfallNotSetException
import no.nav.klage.oppgave.exceptions.VedtakFinalizedException
import no.nav.klage.oppgave.util.AttachmentValidator
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
@Transactional
class VedtakService(
    private val klagebehandlingService: KlagebehandlingService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val attachmentValidator: AttachmentValidator,
    private val joarkClient: JoarkClient,
    private val safClient: SafGraphQlClient,
    private val tilgangService: TilgangService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Transactional(readOnly = true)
    fun getVedtak(klagebehandling: Klagebehandling, vedtakId: UUID): Vedtak {
        return klagebehandling.getVedtak(vedtakId)
    }

    fun setUtfall(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        utfall: Utfall?,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val event =
            klagebehandling.setUtfallInVedtak(vedtakId, utfall, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setGrunn(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        grunn: Grunn?,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val event =
            klagebehandling.setGrunnInVedtak(vedtakId, grunn, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setHjemler(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        hjemler: Set<Hjemmel>,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val event =
            klagebehandling.setHjemlerInVedtak(vedtakId, hjemler, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setJournalpostIdOgOpplastet(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        journalpostId: String?,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val event =
            klagebehandling.setJournalpostIdOgOpplastetInVedtak(vedtakId, journalpostId, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling

    }

    fun markerVedtakSomFerdigstilt(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val event =
            klagebehandling.setVedtakFerdigstiltIJoark(vedtakId, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun slettFilTilknyttetVedtak(
        klagebehandlingId: UUID,
        vedtakId: UUID,
        input: VedtakSlettVedleggInput,
        innloggetIdent: String
    ): Klagebehandling {
        val klagebehandling = klagebehandlingService.getKlagebehandlingForUpdate(
            klagebehandlingId,
            input.klagebehandlingVersjon
        )

        tilgangService.verifySaksbehandlersTilgangTilEnhet(klagebehandling.tildeling!!.enhet!!)

        val vedtak = klagebehandling.getVedtak(vedtakId)

        if (vedtak.journalpostId == null) {
            return klagebehandling
        }

        joarkClient.cancelJournalpost(vedtak.journalpostId!!)

        return setJournalpostIdOgOpplastet(
            klagebehandling,
            vedtak.id,
            null,
            innloggetIdent
        )
    }

    fun oppdaterUtfall(
        klagebehandlingId: UUID,
        vedtakId: UUID,
        input: VedtakUtfallInput,
        innloggetIdent: String
    ): Klagebehandling {
        return setUtfall(
            klagebehandlingService.getKlagebehandlingForUpdate(
                klagebehandlingId,
                input.klagebehandlingVersjon
            ),
            vedtakId,
            input.utfall?.let { Utfall.of(it) },
            innloggetIdent
        )
    }

    fun oppdaterGrunn(
        klagebehandlingId: UUID,
        vedtakId: UUID,
        input: VedtakGrunnInput,
        innloggetIdent: String
    ): Klagebehandling {
        return setGrunn(
            klagebehandlingService.getKlagebehandlingForUpdate(
                klagebehandlingId,
                input.klagebehandlingVersjon
            ),
            vedtakId,
            input.grunn?.let { Grunn.of(it) },
            innloggetIdent
        )
    }

    fun oppdaterHjemler(
        klagebehandlingId: UUID,
        vedtakId: UUID,
        input: VedtakHjemlerInput,
        innloggetIdent: String
    ): Klagebehandling {
        return setHjemler(
            klagebehandlingService.getKlagebehandlingForUpdate(
                klagebehandlingId,
                input.klagebehandlingVersjon
            ),
            vedtakId,
            input.hjemler?.map { Hjemmel.of(it) }?.toSet() ?: emptySet(),
            innloggetIdent
        )
    }

    fun knyttVedtaksFilTilVedtak(
        klagebehandlingId: UUID,
        vedtakId: UUID,
        input: VedtakVedleggInput,
        innloggetIdent: String
    ): Klagebehandling {
        val klagebehandling = klagebehandlingService.getKlagebehandlingForUpdate(
            klagebehandlingId,
            input.klagebehandlingVersjon
        )

        tilgangService.verifySaksbehandlersTilgangTilEnhet(klagebehandling.tildeling!!.enhet!!)

        val vedtak = klagebehandling.getVedtak(vedtakId)

        if (vedtak.ferdigstiltIJoark != null) throw VedtakFinalizedException("Vedtak med id $vedtakId er ferdigstilt")

        return addFileToVedtak(
            klagebehandling,
            vedtak,
            input.vedlegg,
            innloggetIdent
        )
    }


    private fun addFileToVedtak(
        klagebehandling: Klagebehandling,
        vedtak: Vedtak,
        vedlegg: MultipartFile,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        attachmentValidator.validateAttachment(vedlegg)
        if (vedtak.journalpostId != null) {
            joarkClient.cancelJournalpost(vedtak.journalpostId!!)

            setJournalpostIdOgOpplastet(
                klagebehandling,
                vedtak.id,
                null,
                utfoerendeSaksbehandlerIdent
            )
        }

        val journalpostId = joarkClient.createJournalpost(klagebehandling, vedlegg, klagebehandling.tildeling!!.enhet!!)

        return setJournalpostIdOgOpplastet(
            klagebehandling,
            vedtak.id,
            journalpostId,
            utfoerendeSaksbehandlerIdent
        )
    }

    fun ferdigstillVedtak(
        klagebehandlingId: UUID,
        vedtakId: UUID,
        input: VedtakFullfoerInput,
        innloggetIdent: String
    ): Klagebehandling {
        val klagebehandling = klagebehandlingService.getKlagebehandlingForUpdate(
            klagebehandlingId,
            input.klagebehandlingVersjon
        )
        val vedtak = klagebehandling.getVedtak(vedtakId)
        if (vedtak.ferdigstiltIJoark != null) throw VedtakFinalizedException("Vedtak med id $vedtakId er allerede ferdigstilt")
        if (vedtak.journalpostId == null) throw JournalpostNotFoundException("Vedtak med id $vedtakId er ikke journalført")
        if (vedtak.utfall == null) throw UtfallNotSetException("Utfall på vedtak $vedtakId er ikke satt")

        ferdigstillJournalpost(
            klagebehandling,
            vedtak,
            innloggetIdent,
            klagebehandling.tildeling!!.enhet!!
        )
        if (klagebehandling.vedtak.all { it.ferdigstiltIJoark != null }) {
            klagebehandlingService.markerKlagebehandlingSomAvsluttetAvSaksbehandler(klagebehandling, innloggetIdent)
        }
        return klagebehandling
    }

    private fun ferdigstillJournalpost(
        klagebehandling: Klagebehandling,
        vedtak: Vedtak,
        utfoerendeSaksbehandlerIdent: String,
        journalfoerendeEnhet: String
    ): Klagebehandling {
        return try {
            val journalpost = safClient.getJournalpost(vedtak.journalpostId!!)
                ?: throw JournalpostNotFoundException("Journalpost med id ${vedtak.journalpostId} finnes ikke")
            if (journalpost.journalstatus != FERDIGSTILT) {
                joarkClient.finalizeJournalpost(vedtak.journalpostId!!, journalfoerendeEnhet)
            }
            markerVedtakSomFerdigstilt(klagebehandling, vedtak.id, utfoerendeSaksbehandlerIdent)

        } catch (e: Exception) {
            logger.warn("Kunne ikke ferdigstille journalpost ${vedtak.journalpostId}")
            throw JournalpostFinalizationException("Klarte ikke å journalføre vedtak")
        }
    }
}
