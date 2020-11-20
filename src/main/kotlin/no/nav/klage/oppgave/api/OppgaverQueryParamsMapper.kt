package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.domain.OppgaverQueryParams
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.Tilganger
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
        enhetsnr = findEnhetsnrForUser(navIdent)
    )

    private fun findEnhetsnrForUser(navIdent: String): String {
        val tilgangerForSaksbehandler = saksbehandlerRepository.getTilgangerForSaksbehandler(navIdent)
        logWarningIMoreThanOneEnhet(navIdent, tilgangerForSaksbehandler)
        return tilgangerForSaksbehandler.enheter.first().enhetId
    }

    private fun logWarningIMoreThanOneEnhet(navIdent: String, tilgangerForSaksbehandler: Tilganger) {
        if (tilgangerForSaksbehandler.enheter.size > 1) {
            logger.warn(
                "Saksbehandler ({}) had more than one enhet: {}. Only using {}.",
                navIdent,
                tilgangerForSaksbehandler.enheter.map { it.enhetId },
                tilgangerForSaksbehandler.enheter.first().enhetId
            )
        }
    }

}