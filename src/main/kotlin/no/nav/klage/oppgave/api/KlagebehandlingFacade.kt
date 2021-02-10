package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.api.view.KvalitetsvurderingView
import no.nav.klage.oppgave.domain.kodeverk.Grunn
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class KlagebehandlingFacade(
    private val klagebehandlingMapper: KlagebehandlingMapper,
    private val klagebehandlingService: KlagebehandlingService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun getKlagebehandling(oppgaveId: Long): KlagebehandlingView {
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingView(
            klagebehandlingService.getKlagebehandlingByOppgaveId(oppgaveId)
        )
    }

    fun getKvalitetsvurdering(oppgaveId: Long): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.getKlagebehandlingByOppgaveId(oppgaveId)
        )
    }

    fun setKvalitetsvurderingGrunn(oppgaveId: Long, grunn: Grunn): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingGrunn(oppgaveId, grunn)
        )
    }
}