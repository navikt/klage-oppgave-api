package no.nav.klage.oppgave.domain

import org.springframework.http.MediaType

data class ArkivertDokumentWithTitle(
    val title: String,
    val content: ByteArray,
    val contentType: MediaType
)
