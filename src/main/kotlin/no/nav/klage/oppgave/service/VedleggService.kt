package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.joark.JoarkClient
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.util.AttachmentValidator
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class VedleggService(
    private val attachmentValidator: AttachmentValidator,
    private val joarkClient: JoarkClient,
    private val klagebehandlingService: KlagebehandlingService
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun addVedleggSystemUser(klageBehandlingId: UUID, vedlegg: MultipartFile? = null, finalize: Boolean, fagsak: Boolean): String {
        val klagebehandling = klagebehandlingService.getKlagebehandling(klageBehandlingId)
        return if (vedlegg != null) {
            attachmentValidator.validateAttachment(vedlegg)
            joarkClient.createJournalpostWithSystemUser(klagebehandling, vedlegg.bytes, finalize, fagsak)
        } else {
            joarkClient.createJournalpostWithSystemUser(klagebehandling, vedlegg, finalize, fagsak)
        }
    }

    fun updateVedleggSystemUser(klageBehandlingId: UUID, vedlegg: MultipartFile, journalpostId: String): String {
        val klagebehandling = klagebehandlingService.getKlagebehandling(klageBehandlingId)

        attachmentValidator.validateAttachment(vedlegg)
        return joarkClient.updateJournalpostSystemUser(klagebehandling, journalpostId, vedlegg.bytes)

    }

    fun ferdigstillJournalpost(journalpostId: String): String {
        logger.debug("Attempting finalizing of journalpost $journalpostId")
        return joarkClient.finalizeJournalpostSystemUser(journalpostId)
    }

    fun avbrytJournalpost(journalpostId: String): String {
        logger.debug("Attempting cancelling of journalpost $journalpostId")
        return joarkClient.cancelJournalpostSystemUser(journalpostId)
    }
}