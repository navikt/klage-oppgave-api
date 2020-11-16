package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.AxsysClient
import org.springframework.stereotype.Service

@Service
class EnhetRepository(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val axsysClient: AxsysClient
) {

    fun getAnsatteIEnhet(enhetId: String): List<String> {
        return axsysClient.getSaksbehandlereIEnhet(enhetId).map { it.appIdent }
    }

    fun getLedereIEnhet(enhetId: String): List<String> {
        return axsysClient.getSaksbehandlereIEnhet(enhetId)
            .filter { saksbehandlerRepository.erLeder(it.appIdent) }
            .map { it.appIdent }
    }

    fun getFagansvarligeIEnhet(enhetId: String): List<String> {
        return axsysClient.getSaksbehandlereIEnhet(enhetId)
            .filter { saksbehandlerRepository.erFagansvarlig(it.appIdent) }
            .map { it.appIdent }
    }


}