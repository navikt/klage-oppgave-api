package no.nav.klage.oppgave.service.distribusjon

import no.nav.klage.oppgave.domain.klage.BrevMottaker
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Vedtak
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class KlagebehandlingDistribusjonService(
    private val klagebehandlingService: KlagebehandlingService,
    private val vedtakDistribusjonService: VedtakDistribusjonService,
    private val klagebehandlingAvslutningService: KlagebehandlingAvslutningService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Transactional(propagation = Propagation.NEVER)
    fun distribuerKlagebehandling(klagebehandlingId: UUID) {
        try {
            var klagebehandling = klagebehandlingService.getKlagebehandling(klagebehandlingId)
            klagebehandling.vedtak
                .filter { vedtak -> vedtak.erIkkeFerdigDistribuert() }
                .forEach { vedtak ->
                    logger.debug("Vedtak ${vedtak.id} i klagebehandling $klagebehandlingId er ikke distribuert")

                    klagebehandling = lagBrevmottakere(klagebehandling, vedtak)
                    val vedtakMedOppdaterteBrevmottakere = klagebehandling.getVedtak(vedtak.id)
                    vedtakMedOppdaterteBrevmottakere.brevmottakere
                        .filter { brevMottaker -> brevMottaker.erIkkeDistribuertTil() }
                        .forEach { brevMottaker ->
                            logger.debug("Vedtak ${vedtak.id} i klagebehandling $klagebehandlingId er ikke distribuert til brevmottaker ${brevMottaker.id}")

                            klagebehandling =
                                lagJournalpostKopierForSekundaereMottakere(klagebehandling, vedtak.id, brevMottaker)

                            klagebehandling = distribuerVedtakTilBrevmottaker(klagebehandling, vedtak.id, brevMottaker)
                        }

                    klagebehandling = markerVedtakSomFerdigDistribuert(klagebehandling, vedtak.id)
                }

            avsluttKlagebehandling(klagebehandling)
        } catch (e: Exception) {
            logger.error("Feilet under distribuering av klagebehandling $klagebehandlingId", e)
        }
    }

    private fun lagBrevmottakere(
        klagebehandling: Klagebehandling,
        vedtak: Vedtak
    ): Klagebehandling {
        if (vedtak.harIngenBrevMottakere()) {
            logger.debug("Vedtak ${vedtak.id} i klagebehandling ${klagebehandling.id} har ingen brevmottakere, vi oppretter det")
            return vedtakDistribusjonService.lagBrevmottakere(klagebehandling.id, vedtak.id)
        }
        return klagebehandling
    }

    private fun lagJournalpostKopierForSekundaereMottakere(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        brevMottaker: BrevMottaker
    ): Klagebehandling {
        val vedtak = klagebehandling.getVedtak(vedtakId)
        logger.debug("Starter distribusjon av vedtak ${vedtak.id} i klagebehandling ${klagebehandling.id} til brevmottaker ${brevMottaker.id}")
        if (brevMottaker.erIkkeHovedMottakerAv(vedtak)) {
            logger.debug("Brevmottaker ${brevMottaker.id} i vedtak ${vedtak.id} i klagebehandling ${klagebehandling.id} er ikke hovedmottaker, så vi må opprette en ny journalpost")
            return vedtakDistribusjonService.lagKopiAvJournalpostForMottaker(klagebehandling, vedtak, brevMottaker)
        }
        return klagebehandling
    }

    private fun distribuerVedtakTilBrevmottaker(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        brevMottaker: BrevMottaker
    ): Klagebehandling {
        val vedtak = klagebehandling.getVedtak(vedtakId)
        logger.debug("Distribuerer vedtak ${vedtakId} i klagebehandling ${klagebehandling.id} til brevmottaker ${brevMottaker.id}")
        return vedtakDistribusjonService.distribuerJournalpostTilMottaker(
            klagebehandling.id, vedtak, brevMottaker
        )
    }

    private fun markerVedtakSomFerdigDistribuert(
        klagebehandling: Klagebehandling,
        vedtakId: UUID
    ): Klagebehandling {
        val vedtak = klagebehandling.getVedtak(vedtakId)
        logger.debug("Markerer vedtak ${vedtak.id} i klagebehandling ${klagebehandling.id} som ferdig distribuert")
        return vedtakDistribusjonService.markerVedtakSomFerdigDistribuert(klagebehandling.id, vedtak.id)
    }

    private fun avsluttKlagebehandling(klagebehandling: Klagebehandling) {
        logger.debug("Alle vedtak i klagebehandling ${klagebehandling.id} er ferdig distribuert, så vi markerer klagebehandlingen som avsluttet")
        klagebehandlingAvslutningService.avsluttKlagebehandling(klagebehandling.id)
    }

    private fun BrevMottaker.erIkkeDistribuertTil() = this.dokdistReferanse == null

    private fun BrevMottaker.erIkkeHovedMottakerAv(vedtak: Vedtak) = this.journalpostId != vedtak.journalpostId

    private fun Vedtak.erIkkeFerdigDistribuert() = ferdigDistribuert == null

    private fun Vedtak.harIngenBrevMottakere(): Boolean =
        brevmottakere.isEmpty()
}




