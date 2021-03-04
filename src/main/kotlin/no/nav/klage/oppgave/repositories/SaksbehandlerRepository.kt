package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.axsys.AxsysClient
import no.nav.klage.oppgave.clients.axsys.Tilganger
import no.nav.klage.oppgave.clients.azure.MicrosoftGraphClient
import no.nav.klage.oppgave.domain.EnhetMedLovligeTemaer
import no.nav.klage.oppgave.domain.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class SaksbehandlerRepository(
    private val client: MicrosoftGraphClient,
    private val axsysClient: AxsysClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        val saksbehandlerNameCache = mutableMapOf<String, String>()

        const val MAX_AMOUNT_IDENTS_IN_GRAPH_QUERY = 15
    }

    fun getTilgangerForSaksbehandler(ident: String): EnheterMedLovligeTemaer =
        axsysClient.getTilgangerForSaksbehandler(ident).mapToInterntDomene()

    fun getNamesForSaksbehandlere(identer: Set<String>): Map<String, String> {
        logger.debug("Fetching names for saksbehandlere from Microsoft Graph")

        val identerNotInCache = identer.toMutableSet()
        identerNotInCache -= saksbehandlerNameCache.keys
        logger.debug("Only fetching identer not in cache: {}", identerNotInCache)

        val chunkedList = identerNotInCache.chunked(MAX_AMOUNT_IDENTS_IN_GRAPH_QUERY)

        val measuredTimeMillis = measureTimeMillis {
            saksbehandlerNameCache += client.getAllDisplayNames(chunkedList)
        }
        logger.debug("It took {} millis to fetch all names", measuredTimeMillis)

        return saksbehandlerNameCache
    }

    fun erFagansvarlig(ident: String): Boolean = TODO()

    fun erLeder(ident: String): Boolean = TODO()

    fun erSaksbehandler(ident: String): Boolean = TODO()

    private fun Tilganger.mapToInterntDomene(): EnheterMedLovligeTemaer =
        EnheterMedLovligeTemaer(this.enheter.map { enhet ->
            EnhetMedLovligeTemaer(
                enhet.enhetId,
                enhet.navn,
                enhet.temaer?.mapNotNull { mapTemaToTemaName(it) } ?: emptyList())
        })

    private fun mapTemaToTemaName(tema: String): Tema? =
        try {
            Tema.of(tema)
        } catch (e: Exception) {
            logger.error("Unable to map Tema $tema", e)
            null
        }
}