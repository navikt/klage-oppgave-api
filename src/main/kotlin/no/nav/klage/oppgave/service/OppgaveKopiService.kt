package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopi
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjon
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjonId
import no.nav.klage.oppgave.repositories.OppgaveKopiRepository
import no.nav.klage.oppgave.repositories.OppgaveKopiVersjonRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OppgaveKopiService(
    val oppgaveKopiRepository: OppgaveKopiRepository,
    val oppgaveKopiVersjonRepository: OppgaveKopiVersjonRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun lagreOppgaveKopi(oppgaveKopi: OppgaveKopi) {
        logger.debug("Received oppgavekopi with id ${oppgaveKopi.id} and versjon ${oppgaveKopi.versjon} for storing")
        if (oppgaveKopiRepository.existsById(oppgaveKopi.id)) {
            val existingOppgaveKopi = oppgaveKopiRepository.getOne(oppgaveKopi.id)
            if (existingOppgaveKopi.versjon < oppgaveKopi.versjon) {
                oppgaveKopiRepository.save(oppgaveKopi)
            } else {
                logger.debug("Oppgavekopi with id ${existingOppgaveKopi.id} and versjon ${existingOppgaveKopi.versjon} stored before, won't overwrite")
            }
        } else {
            oppgaveKopiRepository.save(oppgaveKopi)
        }

        oppgaveKopiVersjonRepository.save(oppgaveKopi.toVersjon())
    }

    fun hentOppgaveKopi(oppgaveKopiId: Long): OppgaveKopi {
        return oppgaveKopiRepository.getOne(oppgaveKopiId)
    }

    fun hentOppgaveKopiVersjon(oppgaveKopiId: Long, versjon: Int): OppgaveKopiVersjon {
        return oppgaveKopiVersjonRepository.getOne(OppgaveKopiVersjonId(oppgaveKopiId, versjon))
    }
    
    fun hentOppgaveKopiSisteVersjon(oppgaveKopiId: Long): OppgaveKopiVersjon {
        return oppgaveKopiVersjonRepository.findFirstByIdOrderByVersjonDesc(oppgaveKopiId)
    }
}