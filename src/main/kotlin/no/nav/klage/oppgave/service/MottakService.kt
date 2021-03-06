package no.nav.klage.oppgave.service


import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.norg2.Norg2Client
import no.nav.klage.oppgave.config.incrementMottattKlage
import no.nav.klage.oppgave.domain.events.MottakLagretEvent
import no.nav.klage.oppgave.domain.klage.Klager
import no.nav.klage.oppgave.domain.klage.Mottak
import no.nav.klage.oppgave.domain.klage.PartId
import no.nav.klage.oppgave.domain.kodeverk.*
import no.nav.klage.oppgave.exceptions.DuplicateOversendelseException
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.exceptions.OversendtKlageNotValidException
import no.nav.klage.oppgave.repositories.EnhetRepository
import no.nav.klage.oppgave.repositories.MottakRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.klage.oppgave.util.isValidFnrOrDnr
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class MottakService(
    environment: Environment,
    private val mottakRepository: MottakRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val dokumentService: DokumentService,
    private val norg2Client: Norg2Client,
    private val enhetRepository: EnhetRepository,
    private val meterRegistry: MeterRegistry
) {

    private val lovligeTemaerIKabal = LovligeTemaer.lovligeTemaer(environment)
    private val lovligeTyperIKabal = LovligeTyper.lovligeTyper(environment)


    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Transactional
    fun createMottakForKlage(oversendtKlage: OversendtKlage) {
        secureLogger.debug("Prøver å lagre oversendtKlage: {}", oversendtKlage)
        oversendtKlage.validate()

        val mottak = mottakRepository.save(oversendtKlage.toMottak())

        secureLogger.debug("Har lagret mottak basert på oversendtKlage {}", oversendtKlage)
        logger.debug("Har lagret mottak {}, publiserer nå event", mottak.id)

        applicationEventPublisher.publishEvent(MottakLagretEvent(mottak))

        //TODO: Move to outside of transaction to make sure it went well
        meterRegistry.incrementMottattKlage(oversendtKlage.kilde.name, oversendtKlage.tema.navn)
    }

    fun createMottakFromKvalitetsvurdering(kvalitetsvurdering: KvalitetsvurderingManuellInput): UUID {
        val klager = Klager(
            partId = PartId(
                type = PartIdType.PERSON,
                value = kvalitetsvurdering.foedselsnummer
            )
        )

        val mottak = mottakRepository.save(
            Mottak(
                tema = kvalitetsvurdering.tema,
                klager = klager,
                kildeReferanse = "N/A",
                kildesystem = Fagsystem.MANUELL,
                oversendtKaDato = kvalitetsvurdering.datoMottattKlageinstans,
                type = Type.KLAGE
            )
        )

        return mottak.id
    }

    fun OversendtKlage.validate() {
        validateDuplicate(kilde, kildeReferanse)
        tilknyttedeJournalposter.forEach { validateJournalpost(it.journalpostId) }
        validatePartId(klager.id)
        sakenGjelder?.run { validatePartId(sakenGjelder.id) }
        validateTema(tema)
        validateType(type)
        validateEnhet(avsenderEnhet)
        validateSaksbehandler(avsenderSaksbehandlerIdent, avsenderEnhet)
    }

    private fun validateDuplicate(kildeFagsystem: KildeFagsystem, kildeReferanse: String) {
        if (mottakRepository.existsByKildesystemAndKildeReferanse(kildeFagsystem.mapFagsystem(), kildeReferanse)) {
            val message =
                "Kunne ikke lagre oversendelse grunnet duplikat: kilde ${kildeFagsystem.name} og kildereferanse: $kildeReferanse"
            logger.warn(message)
            throw DuplicateOversendelseException(message)
        }
    }

    private fun validateType(type: Type) {
        if (!lovligeTyperIKabal.contains(type)) {
            throw OversendtKlageNotValidException("Kabal kan ikke motta klager med type $type ennå")
        }
    }

    private fun validateTema(tema: Tema) {
        if (!lovligeTemaerIKabal.contains(tema)) {
            throw OversendtKlageNotValidException("Kabal kan ikke motta klager med tema $tema ennå")
        }
    }

    private fun validateSaksbehandler(saksbehandlerident: String, enhet: String) {
        if (enhetRepository.getAnsatteIEnhet(enhet).none { it == saksbehandlerident }) {
            throw OversendtKlageNotValidException("$saksbehandlerident er ikke saksbehandler i enhet $enhet")
        }
    }

    private fun validateEnhet(enhet: String) =
        try {
            norg2Client.fetchEnhet(enhet).navn
        } catch (e: RuntimeException) {
            logger.warn("Unable to validate enhet from oversendt klage: {}", enhet, e)
            throw OversendtKlageNotValidException("$enhet er ikke en gyldig NAV-enhet")
        }

    private fun validateJournalpost(journalpostId: String) =
        try {
            dokumentService.validateJournalpostExistsAsSystembruker(journalpostId)
        } catch (e: JournalpostNotFoundException) {
            logger.warn("Unable to validate journalpost from oversendt klage: {}", journalpostId, e)
            throw OversendtKlageNotValidException("$journalpostId er ikke en gyldig journalpost referanse")
        }

    private fun validatePartId(partId: OversendtPartId) {
        if (partId.type == OversendtPartIdType.VIRKSOMHET) {
            return
        }

        if (!isValidFnrOrDnr(partId.verdi)) {
            throw OversendtKlageNotValidException("Ugyldig fødselsnummer")
        }
    }
}
