package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.joark.JoarkClient
import no.nav.klage.oppgave.clients.saf.rest.ArkivertDokument
import no.nav.klage.oppgave.domain.kafka.KlagevedtakFattet
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setFinalizedIdInVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setJournalpostIdInVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setUtfallInVedtak
import no.nav.klage.oppgave.domain.klage.Vedtak
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.exceptions.VedtakFinalizedException
import no.nav.klage.oppgave.exceptions.VedtakNotFoundException
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.util.AttachmentValidator
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
@Transactional
class VedtakService(
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val vedtakKafkaProducer: VedtakKafkaProducer,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val attachmentValidator: AttachmentValidator,
    private val joarkClient: JoarkClient,
    private val dokumentService: DokumentService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Transactional(readOnly = true)
    fun getVedtak(klagebehandling: Klagebehandling, vedtakId: UUID): Vedtak {
        return getVedtakFromKlagebehandling(klagebehandling, vedtakId)
    }

    fun setUtfall(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        utfall: Utfall,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak {
        val event =
            klagebehandling.setUtfallInVedtak(vedtakId, utfall, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return getVedtakFromKlagebehandling(klagebehandling, vedtakId)
    }

    fun setJournalpostId(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        journalpostId: String,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak {
        val event =
            klagebehandling.setJournalpostIdInVedtak(vedtakId, journalpostId, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return getVedtakFromKlagebehandling(klagebehandling, vedtakId)
    }

    fun setFinalized(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak {
        val event =
            klagebehandling.setFinalizedIdInVedtak(vedtakId, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return getVedtakFromKlagebehandling(klagebehandling, vedtakId)
    }

    fun finalizeJournalpost(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak? {
        val vedtak = getVedtakFromKlagebehandling(klagebehandling, vedtakId)
        if (vedtak.finalized != null) throw VedtakFinalizedException("Vedtak med id $vedtakId er allerede ferdigstilt")
        if (vedtak.journalpostId == null) throw JournalpostNotFoundException("Vedtak med id $vedtakId er ikke journalført")
        return try {
            joarkClient.finalizeJournalpost(vedtak.journalpostId!!)
            setFinalized(klagebehandling, vedtakId, utfoerendeSaksbehandlerIdent)
        } catch (e: Exception) {
            logger.warn("Kunne ikke ferdigstille journalpost ${vedtak.journalpostId}")
            null
        }
    }

    fun addVedlegg(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        vedlegg: MultipartFile,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak {
        val vedtak = getVedtakFromKlagebehandling(klagebehandling, vedtakId)
        if (vedtak.finalized != null) throw VedtakFinalizedException("Vedtak med id $vedtakId er ferdigstilt")
        attachmentValidator.validateAttachment(vedlegg)
        if (vedtak.journalpostId != null) {
            joarkClient.cancelJournalpost(vedtak.journalpostId!!)
        }

        return setJournalpostId(
            klagebehandling,
            vedtakId,
            joarkClient.createJournalpost(klagebehandling, vedlegg),
            utfoerendeSaksbehandlerIdent
        )
    }

    fun getVedlegg(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        utfoerendeSaksbehandlerIdent: String
    ): ArkivertDokument? {
        val vedtak = getVedtakFromKlagebehandling(klagebehandling, vedtakId)
        return if (vedtak.journalpostId != null) {
            dokumentService.getMainDokument(vedtak.journalpostId!!)
        } else {
            null
        }
    }

    fun dispatchVedtakToKafka(klagebehandlingId: UUID, vedtakId: UUID) {
        val klage = klagebehandlingRepository.findById(klagebehandlingId).orElseThrow()
        val vedtak = klage.vedtak.find { it.id == vedtakId }
        require(vedtak != null) { "Fant ikke vedtak på klage" }
        require(vedtak.utfall != null) { "Utfall på vedtak må være satt" }
        require(vedtak.journalpostId != null) { "Kan ikke fullføre et vedtak uten journalført vedtaksbrev" }
        val utfall = vedtak.utfall!!
        val vedtakFattet = KlagevedtakFattet(
            kildeReferanse = klage.kildeReferanse ?: "UKJENT",
            kilde = klage.kilde,
            utfall = utfall,
            vedtaksbrevReferanse = vedtak.journalpostId,
            kabalReferanse = vedtakId.toString()
        )

        vedtakKafkaProducer.sendVedtak(vedtakFattet)
    }

    private fun getVedtakFromKlagebehandling(klagebehandling: Klagebehandling, vedtakId: UUID): Vedtak {
        return klagebehandling.vedtak.firstOrNull {
            it.id == vedtakId
        } ?: throw VedtakNotFoundException("Vedtak med id $vedtakId ikke funnet")
    }
}
