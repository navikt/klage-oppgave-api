package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.DokumentReferanse
import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.clients.saf.graphql.*
import no.nav.klage.oppgave.clients.saf.rest.ArkivertDokument
import no.nav.klage.oppgave.clients.saf.rest.SafRestClient
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Saksdokument
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DokumentService(
    private val safGraphQlClient: SafGraphQlClient,
    private val safRestClient: SafRestClient
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val dokumentMapper = DokumentMapper()
    }

    fun fetchDokumentlisteForKlagebehandling(
        klagebehandling: Klagebehandling,
        pageSize: Int,
        previousPageRef: String?
    ): DokumenterResponse {
        if (klagebehandling.sakenGjelder.erPerson()) {
            val dokumentoversiktBruker: DokumentoversiktBruker =
                safGraphQlClient.getDokumentoversiktBruker(
                    klagebehandling.sakenGjelder.partId.value,
                    pageSize,
                    previousPageRef
                )
            return DokumenterResponse(
                dokumenter = dokumentoversiktBruker.journalposter.map { journalpost ->
                    dokumentMapper.mapJournalpostToDokumentReferanse(journalpost, klagebehandling)
                },
                pageReference = if (dokumentoversiktBruker.sideInfo.finnesNesteSide) {
                    dokumentoversiktBruker.sideInfo.sluttpeker
                } else {
                    null
                }
            )
        } else {
            return DokumenterResponse(dokumenter = emptyList(), pageReference = null)
        }
    }

    fun fetchJournalposterConnectedToKlagebehandling(klagebehandling: Klagebehandling): DokumenterResponse {
        return klagebehandling.saksdokumenter
            .mapNotNull { safGraphQlClient.getJournalpost(it.journalpostId) }
            .map { dokumentMapper.mapJournalpostToDokumentReferanse(it, klagebehandling) }
            .let { DokumenterResponse(dokumenter = it, pageReference = null) }
    }

    fun validateJournalpostExists(journalpostId: String) {
        try {
            safGraphQlClient.getJournalpost(journalpostId)
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            null
        } ?: throw JournalpostNotFoundException("Journalpost $journalpostId not found")
    }

    fun validateJournalpostExistsAsSystembruker(journalpostId: String) {
        try {
            safGraphQlClient.getJournalpostAsSystembruker(journalpostId)
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            null
        } ?: throw JournalpostNotFoundException("Journalpost $journalpostId not found")
    }

    fun fetchDokumentInfoIdForJournalpostAsSystembruker(journalpostId: String): List<String> {
        return try {
            val journalpost = safGraphQlClient.getJournalpostAsSystembruker(journalpostId)
            journalpost?.dokumenter?.filter { harArkivVariantformat(it) }?.map { it.dokumentInfoId } ?: emptyList()
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            emptyList()
        }
    }

    fun getArkivertDokument(journalpostId: String, dokumentInfoId: String): ArkivertDokument {
        return safRestClient.getDokument(dokumentInfoId, journalpostId)
    }

    fun getMainDokument(journalpostId: String): ArkivertDokument {
        val dokumentInfoId = fetchDokumentInfoIdForJournalpostAsSystembruker(journalpostId)
        return getArkivertDokument(journalpostId, dokumentInfoId.first())
    }

    private fun harArkivVariantformat(dokumentInfo: DokumentInfo): Boolean =
        dokumentInfo.dokumentvarianter.any { dv ->
            dv.variantformat == Variantformat.ARKIV
        }

}

class DokumentMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //TODO: Har ikke tatt høyde for skjerming, ref https://confluence.adeo.no/pages/viewpage.action?pageId=320364687
    fun mapJournalpostToDokumentReferanse(
        journalpost: Journalpost,
        klagebehandling: Klagebehandling
    ): DokumentReferanse {

        val hoveddokument = journalpost.dokumenter?.firstOrNull()
            ?: throw RuntimeException("Could not find hoveddokument for journalpost ${journalpost.journalpostId}")

        val dokumentReferanse = DokumentReferanse(
            tittel = hoveddokument.tittel,
            tema = journalpost.tema?.name,
            registrert = journalpost.datoOpprettet.toLocalDate(),
            dokumentInfoId = hoveddokument.dokumentInfoId,
            journalpostId = journalpost.journalpostId,
            harTilgangTilArkivvariant = harTilgangTilArkivvariant(hoveddokument),
            valgt = klagebehandling.saksdokumenter.containsDokument(
                journalpost.journalpostId,
                hoveddokument.dokumentInfoId
            )
        )

        dokumentReferanse.vedlegg.addAll(getVedlegg(journalpost, klagebehandling))

        return dokumentReferanse
    }

    private fun getVedlegg(
        journalpost: Journalpost,
        klagebehandling: Klagebehandling
    ): List<DokumentReferanse.VedleggReferanse> {
        return if (journalpost.dokumenter?.size ?: 0 > 1) {
            journalpost.dokumenter?.subList(1, journalpost.dokumenter.size)?.map { vedlegg ->
                DokumentReferanse.VedleggReferanse(
                    tittel = vedlegg.tittel,
                    dokumentInfoId = vedlegg.dokumentInfoId,
                    harTilgangTilArkivvariant = harTilgangTilArkivvariant(vedlegg),
                    valgt = klagebehandling.saksdokumenter.containsDokument(
                        journalpost.journalpostId,
                        vedlegg.dokumentInfoId
                    )
                )
            } ?: throw RuntimeException("could not create VedleggReferanser from dokumenter")
        } else {
            emptyList()
        }
    }

    private fun harTilgangTilArkivvariant(dokumentInfo: DokumentInfo?): Boolean =
        dokumentInfo?.dokumentvarianter?.any { dv ->
            dv.variantformat == Variantformat.ARKIV && dv.saksbehandlerHarTilgang
        } == true

    private fun MutableSet<Saksdokument>.containsDokument(journalpostId: String, dokumentInfoId: String) =
        any {
            it.journalpostId == journalpostId && it.dokumentInfoId == dokumentInfoId
        }
}
