package no.nav.klage.oppgave.api.mapper

import no.nav.klage.oppgave.api.view.OppgaverQueryParams
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.exceptions.NotOwnEnhetException
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service

@Service
class OppgaverQueryParamsMapper(private val saksbehandlerRepository: SaksbehandlerRepository) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun toSearchCriteria(navIdent: String, oppgaverQueryParams: OppgaverQueryParams) = OppgaverSearchCriteria(
        typer = oppgaverQueryParams.typer,
        ytelser = oppgaverQueryParams.ytelser,
        hjemler = oppgaverQueryParams.hjemler,
        order = if (oppgaverQueryParams.rekkefoelge == OppgaverQueryParams.Rekkefoelge.SYNKENDE) {
            OppgaverSearchCriteria.Order.DESC
        } else {
            OppgaverSearchCriteria.Order.ASC
        },
        offset = oppgaverQueryParams.start,
        limit = oppgaverQueryParams.antall,
        erTildeltSaksbehandler = oppgaverQueryParams.erTildeltSaksbehandler,
        saksbehandler = oppgaverQueryParams.tildeltSaksbehandler,
        projection = oppgaverQueryParams.projeksjon?.let { OppgaverSearchCriteria.Projection.valueOf(it.name) },
        enhetsnr = validateAndGetEnhetId(navIdent, oppgaverQueryParams.enhetId)
    )

    private fun validateAndGetEnhetId(navIdent: String, enhetId: String): String {
        val tilgangerForSaksbehandler = saksbehandlerRepository.getTilgangerForSaksbehandler(navIdent)

        if (tilgangerForSaksbehandler.enheter.none { e -> e.enhetId == enhetId }) {
            throw NotOwnEnhetException("$navIdent is not part of enhet $enhetId")
        }
        return enhetId
    }

}