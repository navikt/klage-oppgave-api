# Integrere med Kabal
![](klage_teknisk.png "Dataflyt")

## Kort fortalt

- Klage kommer inn via skjema på nav.no
- Klage arkiveres i JOARK
- Melding om innkommen klage sendes på JOARK sitt kafka-topic
- Førsteinstans håndterer klage i sin vedtaksløsning
- Dersom klage skal videre til klageinstans gjøres en POST på `kabal-api`
- Når klage er ferdig behandlet på klageinstans, legges en melding på et kafka-topic

### Oversende klage til Kabal

````
POST <kabal-api-url>/oversendelse/klage <oversendt-klage-json>
````
- DEV-url: `https://kabal-api.dev.nav.no`
- PROD-url: `https://kabal-api.intern.nav.no`

Skjema for oversendt klage kan finnes [her](../schema/oversendt-klage.json).

Eksempel
````
{
    "uuid": "123e4567-e89b-12d3-a456-426614174000",
    "tema": "SYK",
    "eksternReferanse": "1234",
    "innsynUrl": "https://sykepenger.intern.nav.no/behandling/1234",
    "foedselsnummer": "12345678910",
    "beskrivelse": "Enkel eskrivelse",
    "avsenderSaksbehandlerIdent": "Z123456",
    "avsenderEnhet": "2334",
    "hjemler": ["8-4", "8-5"],
    "mottattFoersteinstans": "2020-01-01",
    "innsendtTilNav": "2019-12-20",
    "sakstype": "KLAGE",
    "oversendtEnhet": "4291",
    "oversendelsesbrevJournalpostId": "5766798",
    "brukersKlageJournalpostId": "578654",
    "frist": "2021-12-12",
    "kilde": "SPEIL",
    "mottakerAdresse": {
        "adressetype": "NORSK/UTENLANDSK",
        "adresselinje1": "Påkrevd hvis adresse er utenlandsk",
        "adresselinje2": "Adresselinje 2",
        "adresselinje3": "Adresselinje 3",
        "postnummer": "Påkrevd hvis adresse er norsk",
        "poststed": "Påkrevd hvis adresse er norsk",
        "land": "iso3166-1 alfa-2"
    }
}
````

### Klage-vedtak på Kafka

Skjema for klage-vedtak sendt på `klage.vedtak-fattet.v1` kan finnes [her](../schema/klagevedtak-fattet.json).

Eksempel
````
{
    "id": "1234",
    "utfall": "MEDHOLD",
    "vedtaksbrevReferanse": "34567657"
}
````
