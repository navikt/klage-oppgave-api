package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.gosys.hjemler
import org.springframework.stereotype.Service

@Service
class HjemmelParsingService {

    private val hjemmelRegex = """(\d{1,2}-\d{1,2})+""".toRegex()

    fun extractHjemmel(text: String): List<String> = hjemmelRegex.findAll(text).collect()

    private fun Sequence<MatchResult>.collect(): List<String> {
        val list = mutableListOf<String>()
        this.iterator().forEachRemaining {
            val hjemmel = it.value.replace("§", "").trim()
            if (hjemmel.isValidHjemmel()) {
                list.add(hjemmel)
            }
        }
        return list
    }

    private fun String.isValidHjemmel(): Boolean = hjemler.contains(this)

}
