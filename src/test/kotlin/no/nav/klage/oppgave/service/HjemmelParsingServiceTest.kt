package no.nav.klage.oppgave.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class HjemmelParsingServiceTest {

    private val service = HjemmelParsingService()

    @Test
    fun `enkel beskrivelse med en paragraf gir enkel paragraf`() {
        val hjemmelListe = service.extractHjemmel(beskrivelseParagrafSimple)
        hjemmelListe hasSize 1
        hjemmelListe firstParagrafIs "8-3"
    }

    @Test
    fun `enkel beskrivelse med høye paragraftall gir enkel paragraf`() {
        val hjemmelListe = service.extractHjemmel(beskrivelseParagrafSimpleHigh)
        hjemmelListe hasSize 1
        hjemmelListe firstParagrafIs "22-15"
    }

    @Test
    fun `paragraf med space gir enkel paragraf`() {
        val hjemmelListe = service.extractHjemmel(beskrivelseParagrafSpace)
        hjemmelListe hasSize 1
        hjemmelListe firstParagrafIs "22-3"
    }

    @Test
    fun `flere paragrafer i tekst gir flere paragrafer i liste`() {
        val hjemmelListe = service.extractHjemmel(beskrivelseParagrafSeveral)
        hjemmelListe hasSize 5
        hjemmelListe firstParagrafIs "8-3"
    }

    @Test
    fun `flere paragrafer i tekst med space gir flere paragrafer i liste`() {
        val hjemmelListe = service.extractHjemmel(beskrivelseParagrafSeveralSpace)
        hjemmelListe hasSize 2
        hjemmelListe firstParagrafIs "8-3"
    }

    @Test
    fun `flere paragrafer i tekst med space og ord gir flere paragrafer i liste`() {
        val hjemmelListe = service.extractHjemmel(beskrivelseParagrafSpaceAndWord)
        hjemmelListe hasSize 2
        hjemmelListe firstParagrafIs "8-3"
    }

    private infix fun List<String>.hasSize(size: Int) {
        Assertions.assertEquals(size, this.size)
    }

    private infix fun List<String>.firstParagrafIs(expected: String) {
        Assertions.assertEquals(expected, this[0])
    }

    private val beskrivelseParagrafSimple = """
        Beskrivelsehistorikk
        --- 09.09.2020 16:36 Saksbehandler, Engasjert (S123456, 4291) ---
        §8-3
        --- 08.07.2020 10:13 Saksbehandler, Engasjert (S123456, 4291) ---
        Oppgaven er flyttet   fra mappe <ingen> til Sykepenger klager 
    """.trimIndent()

    private val beskrivelseParagrafSimpleHigh = """
        Beskrivelsehistorikk
        --- 09.09.2020 16:36 Saksbehandler, Engasjert (S123456, 4291) ---
        §22-15
        --- 08.07.2020 10:13 Saksbehandler, Engasjert (S123456, 4291) ---
        Oppgaven er flyttet   fra mappe <ingen> til Sykepenger klager 
    """.trimIndent()

    private val beskrivelseParagrafSeveral = """
        Beskrivelsehistorikk
        --- 17.09.2020 13:46 Saksbehandler, Engasjert (S123456, 4291) ---
        §8-3,-22-15
        --- 03.09.2020 14:13 Saksbehandler, Engasjert (S123456, 4291) ---
        8-3,-22-15
        --- 08.10.2019 11:52 Saksbehandler, Engasjert (S123456, 4291) ---
        §8-4
        Oppgaven er flyttet , fra saksbehandler <ingen> til S123456, fra mappe <ingen> til Sykepenger klager 
    """.trimIndent()

    private val beskrivelseParagrafSpace = """
        Beskrivelsehistorikk
        --- 02.09.2020 08:32 Saksbehandler, Engasjert (S123456, 4291) ---
        § 22-3
        Oppgaven er flyttet , fra saksbehandler <ingen> til S123456, fra mappe <ingen> til Sykepenger klager 
    """.trimIndent()

    private val beskrivelseParagrafSeveralSpace = """
        Beskrivelsehistorikk
        --- 26.07.2020 15:05 Saksbehandler, Engasjert (S123456, 4291) ---
        §8-3 8-4. Ber om at saken prioriteres da saken skal videre til NAV kontroll for vurdering av mulig straffesak
        Oppgaven er flyttet  fra saksbehandler S123456 til S123456 
    """.trimIndent()

    private val beskrivelseParagrafSpaceAndWord = """
        Beskrivelsehistorikk
        --- 26.06.2020 09:26 Saksbehandler, Engasjert (S123456, 4291) ---
        §§ 8-3 og 8-35.
        Oppgaven er flyttet , fra saksbehandler <ingen> til S123456, fra mappe <ingen> til Sykepenger klager 
    """.trimIndent()

}
