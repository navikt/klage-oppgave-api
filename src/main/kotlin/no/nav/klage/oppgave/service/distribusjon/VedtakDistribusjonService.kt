package no.nav.klage.oppgave.service.distribusjon

import no.nav.klage.oppgave.clients.dokdistfordeling.DokDistFordelingClient
import no.nav.klage.oppgave.domain.klage.BrevMottaker
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setDokdistReferanseInVedtaksmottaker
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setVedtakFerdigDistribuert
import no.nav.klage.oppgave.domain.klage.Vedtak
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class VedtakDistribusjonService(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val dokDistFordelingClient: DokDistFordelingClient,
    private val klagebehandlingService: KlagebehandlingService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        const val SYSTEMBRUKER = "SYSTEMBRUKER" //TODO ??
    }

    @Transactional
    fun distribuerJournalpostTilMottaker(
        klagebehandlingId: UUID,
        vedtak: Vedtak,
        mottaker: BrevMottaker
    ): Klagebehandling {
        try {
            val klagebehandling =
                klagebehandlingService.getKlagebehandlingForUpdateBySystembruker(klagebehandlingId, null)
            val dokdistReferanse: UUID =
                dokDistFordelingClient.distribuerJournalpost(vedtak.journalpostId!!).bestillingsId
            setDokdistReferanse(klagebehandling, vedtak.id, mottaker.id, dokdistReferanse, SYSTEMBRUKER)
            return klagebehandling
        } catch (e: Exception) {
            logger.warn("Kunne ikke distribuere journalpost ${vedtak.journalpostId}")
            throw e
        }
    }

    private fun setDokdistReferanse(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        mottakerId: UUID,
        dokdistReferanse: UUID,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak {
        val event = klagebehandling.setDokdistReferanseInVedtaksmottaker(
            vedtakId,
            mottakerId,
            dokdistReferanse,
            utfoerendeSaksbehandlerIdent
        )
        applicationEventPublisher.publishEvent(event)
        return klagebehandling.getVedtak(vedtakId)
    }

    @Transactional
    fun lagBrevmottakere(klagebehandlingId: UUID, vedtakId: UUID): Klagebehandling {
        val klagebehandling = klagebehandlingService.getKlagebehandlingForUpdateBySystembruker(klagebehandlingId, null)
        logger.debug("Lager brevmottakere for vedtak $vedtakId i klagebehandling ${klagebehandling.id}")
        klagebehandling.lagBrevmottakereForVedtak(vedtakId)
        return klagebehandling
    }

    @Transactional
    fun lagKopiAvJournalpostForMottaker(
        klagebehandling: Klagebehandling,
        vedtak: Vedtak,
        brevMottaker: BrevMottaker
    ): Klagebehandling {
        //TODO: Kod opp dette
        throw IllegalStateException("Dette har vi ikke kodet opp ennå, K9 støtter bare en mottaker")
    }

    @Transactional
    fun markerVedtakSomFerdigDistribuert(klagebehandlingId: UUID, vedtakId: UUID): Klagebehandling {
        val klagebehandling = klagebehandlingService.getKlagebehandlingForUpdateBySystembruker(klagebehandlingId, null)
        val event = klagebehandling.setVedtakFerdigDistribuert(vedtakId, SYSTEMBRUKER)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }
}
