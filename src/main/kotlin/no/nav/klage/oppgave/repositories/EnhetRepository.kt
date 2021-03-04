package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.axsys.AxsysClient
import org.springframework.stereotype.Service

@Service
class EnhetRepository(
    private val axsysClient: AxsysClient
) {

    fun getAnsatteIEnhet(enhetId: String): List<String> {
        return axsysClient.getSaksbehandlereIEnhet(enhetId).map { it.appIdent }
    }

    fun getLedereIEnhet(enhetId: String): List<String> {
        TODO()
    }

    fun getFagansvarligeIEnhet(enhetId: String): List<String> {
        TODO()
    }

}