package no.nav.klage.oppgave.service

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class OverfoeringsdataParserService {

    private val newCommentRegex =
        Regex(
            """\-\-\-\s(\d\d\.\d\d\.\d\d\d\d)\s\d\d\:\d\d\s[^(]*\s\(([^,]*)\,\s(\d{4})\)\s\-\-\-""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)
        )

    private val enhetOverfoeringRegex =
        Regex(""".*?fra\senhet\s(\d{4})\stil\s(42\d\d).*?(?=${'$'})""")

    fun parseBeskrivelse(beskrivelse: String): Overfoeringsdata? {
        val iterator = beskrivelse.lineSequence().iterator()
        var saksbehandlerWhoMadeTheChange: String? = null
        var enhetOfsaksbehandlerWhoMadeTheChange: String? = null
        var datoForOverfoering: LocalDate? = null
        var enhetOverfoertFra: String? = null
        var enhetOverfoertTil: String? = null

        while (iterator.hasNext() && (saksbehandlerWhoMadeTheChange == null || enhetOfsaksbehandlerWhoMadeTheChange == null || enhetOverfoertFra == null || enhetOverfoertTil == null || datoForOverfoering == null)) {
            val line = iterator.next()
            if (newCommentRegex.containsMatchIn(line)) {
                val values = newCommentRegex.find(line)?.groupValues!!
                datoForOverfoering = LocalDate.parse(values[1], DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                saksbehandlerWhoMadeTheChange = values[2]
                enhetOfsaksbehandlerWhoMadeTheChange = values[3]
            } else if (enhetOverfoeringRegex.containsMatchIn(line)) {
                val values = enhetOverfoeringRegex.find(line)?.groupValues!!
                enhetOverfoertFra = values[1]
                enhetOverfoertTil = values[2]
            }
        }
        return if (saksbehandlerWhoMadeTheChange == null || enhetOfsaksbehandlerWhoMadeTheChange == null || enhetOverfoertFra == null || enhetOverfoertTil == null || datoForOverfoering == null) {
            null
        } else {
            Overfoeringsdata(
                saksbehandlerWhoMadeTheChange,
                enhetOfsaksbehandlerWhoMadeTheChange,
                datoForOverfoering,
                enhetOverfoertFra,
                enhetOverfoertTil
            )
        }
    }
}

data class Overfoeringsdata(
    val saksbehandlerWhoMadeTheChange: String,
    val enhetOfsaksbehandlerWhoMadeTheChange: String,
    val datoForOverfoering: LocalDate,
    val enhetOverfoertFra: String,
    val enhetOverfoertTil: String
)
