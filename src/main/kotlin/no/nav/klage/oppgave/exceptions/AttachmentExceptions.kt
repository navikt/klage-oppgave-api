package no.nav.klage.oppgave.exceptions

class AttachmentEncryptedException(override val message: String = "ENCRYPTED") : RuntimeException()
class AttachmentIsEmptyException(override val message: String = "EMPTY") : RuntimeException()
class AttachmentHasVirusException(override val message: String = "VIRUS") : RuntimeException()
