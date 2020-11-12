package no.nav.klage.oppgave.service

import org.springframework.stereotype.Service

@Service
class HjemmelParsingService {

    private val hjemmelRegex: String = "TODO regex for å finne hjemmel"

    fun extractHjemmel(text: String?): List<String> = listOf()

    private fun isValidHjemmel(hjemmel: String): Boolean = false // TODO Sjekk mot lovdata

}
