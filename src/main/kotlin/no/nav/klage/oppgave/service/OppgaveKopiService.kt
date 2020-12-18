package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopi
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjon
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjonId
import no.nav.klage.oppgave.domain.oppgavekopi.helper.toEsOppgave
import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.repositories.OppgaveKopiRepository
import no.nav.klage.oppgave.repositories.OppgaveKopiVersjonRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OppgaveKopiService(
    val oppgaveKopiRepository: OppgaveKopiRepository,
    val oppgaveKopiVersjonRepository: OppgaveKopiVersjonRepository,
    val elasticsearchRepository: ElasticsearchRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Transactional(readOnly = true)
    fun indexAllOppgaveKopier() {
        var page: Pageable = PageRequest.of(0, 100)
        do {
            val oppgavePage: Page<OppgaveKopi> = oppgaveKopiRepository.findAll(page)

            elasticsearchRepository.save(oppgavePage.content.map { it.toEsOppgave() })
            page = oppgavePage.nextOrLastPageable()
        } while (oppgavePage.hasNext())
    }

    private fun indexOppgaveKopi(oppgaveKopi: OppgaveKopi) {
        try {
            elasticsearchRepository.save(oppgaveKopi.toEsOppgave())
        } catch (e: Exception) {
            logger.warn("Unable to index oppgaveKopi ${oppgaveKopi.id}", e)
        }
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

        indexOppgaveKopi(oppgaveKopi)
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