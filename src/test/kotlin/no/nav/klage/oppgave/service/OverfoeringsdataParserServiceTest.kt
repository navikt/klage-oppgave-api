package no.nav.klage.oppgave.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OverfoeringsdataParserServiceTest {

    private val service = OverfoeringsdataParserService()

    private val beskrivelseMedKunOverfoeringer =
        """
            --- 06.05.2019 13:06 Alvsåker, Odin Lekve (A142467, 4474) ---
            Klage jf. Ftrl. § 21-1 og Fvl. § 28 og § 2 oversendes KA
        
            Oppgaven er flyttet fra enhet 4474 til 4203, fra saksbehandler A142467 til <ingen>, fra mappe <ingen> til <ingen>
        
        
        
            --- 09.10.2020 16:25 Sævdal, Camilla Helene (S156990, 4416) ---
            Klageinnstilling sendt klager. Sak (hel-elektronisk) overført NAV Klageinstans Oslo og Akershus  for videre behandling av klagen.
            Oppgaven er flyttet fra enhet 4416 til 4291, fra saksbehandler S156990 til <ingen>, fra mappe <ingen> til <ingen>
        
        
        
            --- 20.10.2020 13:34 Dahl, Lasse Gausen (D123751, 4418) ---
            Innstilling er skrevet i dag, tilgjengelig i Gosys i morgen
            Oppgaven er flyttet fra enhet 4418 til 4291, fra saksbehandler D123751 til <ingen>, fra mappe <ingen> til <ingen>
        
        
        
            --- 09.03.2020 10:40 Heggen, Ingvild Grotbæk (M139074, 4416) ---
            Klageinnstilling sendt klager. Sak (hel-elektronisk) overført NAV Klageinstans Oslo og Akershus  for videre behandling av klagen.
        
            Oppgaven er flyttet fra enhet 4416 til 4291, fra saksbehandler M139074 til <ingen>, fra mappe <ingen> til <ingen>
        """.trimIndent()

    private val beskrivelseMedLittAvHvert =
        """
            Beskrivelsehistorikk
            --- 18.09.2020 14:44 Nordli, Einar Kyrre (N106819, 4416) ---
            ok
             
            --- 18.09.2020 14:44 Nordli, Einar Kyrre (N106819, 4416) ---
            Oppgaven er flyttet  fra saksbehandler <ingen> til N106819
             
            --- 07.09.2020 17:06 Oksavik, Ingrid Døving (O142054, 4291) ---
            Sak ferdig behandla KA. Stadfesta.
            Oppgaven er flyttet fra enhet 4291 til 4416, fra saksbehandler O142054 til <ingen>, fra mappe <ingen> til <ingen>
             
            --- 11.08.2020 11:27 Oksavik, Ingrid Døving (O142054, 4291) ---
            UB § 8-13
            Oppgaven er flyttet  fra saksbehandler G150538 til O142054
             
            --- 10.03.2020 11:29 Buskerud, Kristin Åsene (B126820, 4291) ---
            §8-13
            Oppgaven er flyttet , fra saksbehandler <ingen> til O142054, fra mappe <ingen> til Sykepenger klager
             
            --- 09.03.2020 10:40 Heggen, Ingvild Grotbæk (M139074, 4416) ---
            Klageinnstilling sendt klager. Sak (hel-elektronisk) overført NAV Klageinstans Oslo og Akershus  for videre behandling av klagen.
             
            Oppgaven er flyttet fra enhet 4416 til 4291, fra saksbehandler M139074 til <ingen>, fra mappe <ingen> til <ingen>
             
            --- 07.03.2020 17:09 Heggen, Ingvild Grotbæk (M139074, 4416) ---
             
            Oppgaven er flyttet  fra saksbehandler <ingen> til M139074
             
            --- 10.02.2020 10:17 Askim, Anders Kristiansen (A147463, 4416) ---
             
            Oppgaven er flyttet   fra mappe <ingen> til 30 Klager- Klar til behandling
             
            --- 10.02.2020 10:17 Askim, Anders Kristiansen (A147463, 4416) ---
             
            Oppgaven er flyttet   fra mappe 30 Klager- Klar til behandling til <ingen>
            Oppgaven har byttet oppgavetype fra Vurder henvendelse til Behandle sak (Manuell)
             
            --- 07.02.2020 12:41 Klefstad, Edith (K105052, 4416) ---
            Ikke omgjøring.
            Oppgaven er flyttet , fra saksbehandler K105052 til <ingen>, fra mappe <ingen> til 30 Klager- Klar til behandling
             
            --- 07.02.2020 10:37 Toresen, Marina (T136784, 4416) ---
            Klage registrert i modia 030220.
             
            Svartidsbrev sendt.
        """.trimIndent()

    @Test
    fun `beskrivelse med kun overfoeringer parsed correctly`() {

        val (saksbehandlerWhoMadeTheChange, enhetOfsaksbehandlerWhoMadeTheChange, datoForOverfoering, enhetOverfoertFra, enhetOverfoertTil) = service.parseBeskrivelse(
            beskrivelseMedKunOverfoeringer
        )!!

        assertThat(saksbehandlerWhoMadeTheChange).isEqualTo("A142467")
        assertThat(enhetOfsaksbehandlerWhoMadeTheChange).isEqualTo("4474")
        assertThat(datoForOverfoering).isEqualTo("2019-05-06")
        assertThat(enhetOverfoertFra).isEqualTo("4474")
        assertThat(enhetOverfoertTil).isEqualTo("4203")

    }

    @Test
    fun `beskrivelse med litt av hvert parsed correctly`() {

        val (saksbehandlerWhoMadeTheChange, enhetOfsaksbehandlerWhoMadeTheChange, datoForOverfoering, enhetOverfoertFra, enhetOverfoertTil) = service.parseBeskrivelse(
            beskrivelseMedLittAvHvert
        )!!

        assertThat(saksbehandlerWhoMadeTheChange).isEqualTo("M139074")
        assertThat(enhetOfsaksbehandlerWhoMadeTheChange).isEqualTo("4416")
        assertThat(datoForOverfoering).isEqualTo("2020-03-09")
        assertThat(enhetOverfoertFra).isEqualTo("4416")
        assertThat(enhetOverfoertTil).isEqualTo("4291")

    }
}