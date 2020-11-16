package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.gosys.hjemler
import org.springframework.stereotype.Service

@Service
class HjemmelParsingService {

    private val hjemmelRegex = """(\d{1,2}-\d{1,2})+""".toRegex()

    fun extractHjemmel(text: String): List<String> = hjemmelRegex.find(text).collect()

    private fun MatchResult?.collect(): List<String> {
        var matchResult = this
        val list = mutableListOf<String>()
        while (matchResult != null) {
            val hjemmel = matchResult.value.replace("§", "").trim()
            if (hjemmel.isValidHjemmel()) {
                list.add(hjemmel)
            }
            matchResult = matchResult.next()
        }
        return list
    }

    private fun String.isValidHjemmel(): Boolean = hjemler.contains(this)

}
