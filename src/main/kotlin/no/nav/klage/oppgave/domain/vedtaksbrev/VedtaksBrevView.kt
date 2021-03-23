package no.nav.klage.oppgave.domain.vedtaksbrev

import no.nav.klage.oppgave.domain.vedtaksbrev.enums.VedtaksBrevMal
import java.util.*

class VedtaksBrevView(
    val klagebehandlingId: UUID,
    val id: UUID? = null,
    val brevMal: VedtaksBrevMal,
    val elements: List<BrevElementView>? = null
)