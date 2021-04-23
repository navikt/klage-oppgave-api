package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setUtfallInVedtak
import no.nav.klage.oppgave.domain.klage.Vedtak
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import no.nav.klage.oppgave.exceptions.VedtakNotFoundException
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class VedtakService(
    private val applicationEventPublisher: ApplicationEventPublisher
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

    private fun getVedtakFromKlagebehandling(klagebehandling: Klagebehandling, vedtakId: UUID): Vedtak {
        return klagebehandling.vedtak.firstOrNull() {
            it.id == vedtakId
        } ?: throw VedtakNotFoundException("Vedtak med id $vedtakId ikke funnet")
    }

}