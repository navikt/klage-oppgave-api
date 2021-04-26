package no.nav.klage.oppgave.util

import no.nav.klage.oppgave.clients.clamav.ClamAvClient
import no.nav.klage.oppgave.exceptions.AttachmentEncryptedException
import no.nav.klage.oppgave.exceptions.AttachmentHasVirusException
import no.nav.klage.oppgave.exceptions.AttachmentIsEmptyException
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.apache.tika.Tika
import org.springframework.http.MediaType
import org.springframework.web.multipart.MultipartFile


class AttachmentValidator(
    private val clamAvClient: ClamAvClient
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun validateAttachment(vedlegg: MultipartFile) {
        logger.debug("Validating attachment.")
        if (vedlegg.isEmpty) {
            logger.warn("Attachment is empty")
            throw AttachmentIsEmptyException()
        }

        if (vedlegg.hasVirus()) {
            logger.warn("Attachment has virus")
            throw AttachmentHasVirusException()
        }

        if (vedlegg.isPDF() && vedlegg.isEncrypted()) {
            logger.warn("Attachment is encrypted")
            throw AttachmentEncryptedException()
        }

        logger.debug("Validation successful.")
    }

    private fun MultipartFile.hasVirus() = !clamAvClient.scan(this.bytes)

    private fun MultipartFile.isEncrypted(): Boolean {
        return try {
            PDDocument.load(this.bytes)
            false
        } catch (ipe: InvalidPasswordException) {
            true
        }
    }

    private fun MultipartFile.isPDF() =
        MediaType.valueOf(Tika().detect(this.bytes)) == MediaType.APPLICATION_PDF
}