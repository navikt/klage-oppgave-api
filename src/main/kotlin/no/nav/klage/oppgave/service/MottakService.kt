package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.OversendtKlage
import no.nav.klage.oppgave.clients.norg2.Norg2Client
import no.nav.klage.oppgave.domain.klage.KLAGEENHET_PREFIX
import no.nav.klage.oppgave.events.MottakLagretEvent
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.exceptions.OversendtKlageNotValidException
import no.nav.klage.oppgave.exceptions.OversendtKlageReceivedBeforeException
import no.nav.klage.oppgave.repositories.EnhetRepository
import no.nav.klage.oppgave.repositories.MottakRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MottakService(
    private val mottakRepository: MottakRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val dokumentService: DokumentService,
    private val norg2Client: Norg2Client,
    private val enhetRepository: EnhetRepository,
    private val hjemmelService: HjemmelService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Transactional
    fun createMottakForKlage(oversendtKlage: OversendtKlage) {
        oversendtKlage.validate()
        val mottak = mottakRepository.save(oversendtKlage.toMottak())
        applicationEventPublisher.publishEvent(MottakLagretEvent(mottak))
    }

    fun OversendtKlage.validate() {
        if (mottakRepository.existsById(uuid)) {
            logger.warn("We have received oversendtKlage with uuid {} before", uuid)
            throw OversendtKlageReceivedBeforeException("Processed and received $uuid before")
        }
        oversendelsesbrevJournalpostId?.let {
            validateJournalpost(it)
        }
        brukersKlageJournalpostId?.let {
            validateJournalpost(it)
        }

        validateEnhet(avsenderEnhet)
        validateSaksbehandler(avsenderSaksbehandlerIdent, avsenderEnhet)
        oversendtEnhet.let {
            validateEnhet(it)
            validateKaEnhet(it)
        }
        hjemler.forEach { validateHjemmel(it) }
    }

    private fun validateSaksbehandler(saksbehandlerident: String, avsenderEnhet: String) {
        if (enhetRepository.getAnsatteIEnhet(avsenderEnhet).none { it == saksbehandlerident }) {
            throw OversendtKlageNotValidException("Angitt saksbehandler er ikke i angitt enhet")
        }
    }

    private fun validateHjemmel(hjemmel: String) =
        try {
            hjemmelService.generateHjemmelFromText(hjemmel)
        } catch (e: Exception) {
            logger.warn("Unable to parse hjemmel from oversendt klage: {}", hjemmel, e)
            throw OversendtKlageNotValidException("Ugyldig hjemmel angitt")
        }


    private fun validateKaEnhet(enhet: String) {
        if (!enhet.startsWith(KLAGEENHET_PREFIX)) {
            logger.warn("{} is not a klageinstansen enhet", enhet)
            throw OversendtKlageNotValidException("Angitt enhet er ikke i klageinstansen")
        }
    }

    private fun validateEnhet(enhet: String) =
        try {
            norg2Client.fetchEnhet(enhet).navn
        } catch (e: RuntimeException) {
            logger.warn("Unable to validate enhet from oversendt klage: {}", enhet, e)
            throw OversendtKlageNotValidException("Ugyldig NAV-enhet angitt")
        }

    private fun validateJournalpost(journalpostId: String) =
        try {
            //TODO: Denne kjører i saksbehandlers context, vi trenger en systembruker her..
            // Det må lages mot SAF, og da må vi vel også gå gjennom apigw.. :-(
            //dokumentService.validateJournalpostExists(journalpostId)
        } catch (e: JournalpostNotFoundException) {
            logger.warn("Unable to validate journalpost from oversendt klage: {}", journalpostId, e)
            throw OversendtKlageNotValidException("Ugyldig journalpost referanse angitt")
        }


}
