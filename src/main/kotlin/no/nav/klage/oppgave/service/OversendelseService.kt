package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.OversendtKlage
import no.nav.klage.oppgave.events.MottakLagretEvent
import no.nav.klage.oppgave.repositories.MottakRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OversendelseService(
    private val mottakRepository: MottakRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun createMottakForKlage(oversendtKlage: OversendtKlage) {
        if (mottakRepository.existsById(oversendtKlage.uuid)) {
            //TODO: Throw exception or log silently? Is this supposed to be idempotent?
            //TODO: Are we supposed to support updates? What if oversendtKlage has the same UUID, but different values?
        }
        val mottak = mottakRepository.save(oversendtKlage.toMottak())
        applicationEventPublisher.publishEvent(MottakLagretEvent(mottak))
    }

}
