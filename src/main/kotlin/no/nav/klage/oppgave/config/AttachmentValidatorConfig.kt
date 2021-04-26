package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.clients.clamav.ClamAvClient
import no.nav.klage.oppgave.util.AttachmentValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AttachmentValidatorConfig(private val clamAvClient: ClamAvClient) {

    @Bean
    fun attachmentValidator(): AttachmentValidator {
        return AttachmentValidator(
            clamAvClient
        )
    }

}