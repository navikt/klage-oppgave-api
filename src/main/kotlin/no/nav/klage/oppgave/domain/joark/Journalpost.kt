package no.nav.klage.oppgave.domain.joark

import no.nav.klage.oppgave.domain.kodeverk.Tema

data class Journalpost(
    val journalposttype: JournalpostType? = null,
    val tema: Tema,
    val behandlingstema: String,
    //TODO: Er dette riktig?
    val kanal: String = "NAV_NO",
    val tittel: String,
    val avsenderMottaker: AvsenderMottaker? = null,
    val journalfoerendeEnhet: String? = null,
    val eksternReferanseId: String? = null,
    val bruker: Bruker? = null,
    val sak: Sak? = null,
    val dokumenter: List<Dokument>? = mutableListOf(),
    val tilleggsopplysninger: List<Tilleggsopplysning> = mutableListOf()
)

enum class JournalpostType {
    INNGAAENDE,
    UTGAAENDE,
    NOTAT
}

data class AvsenderMottaker(
    val id: String,
    val idType: AvsenderMottakerIdType,
    val navn: String? = null,
    val land: String? = null
)

enum class AvsenderMottakerIdType {
    FNR,
    ORGNR,
    HPRNR,
    UTL_ORG
}

data class Bruker(
    val id: String,
    val idType: BrukerIdType
)

enum class BrukerIdType {
    FNR,
    ORGNR,
    AKTOERID
}

data class Sak(
    val sakstype: Sakstype,
    val fagsaksystem: FagsaksSystem? = null,
    val fagsakid: String? = null,
    val arkivsaksystem: ArkivsaksSystem? = null,
    val arkivsaksnummer: String? = null
)

enum class Sakstype {
    FAGSAK,
    GENERELL_SAK,
    ARKIVSAK
}

enum class FagsaksSystem {
    AO01,
    AO11,
    BISYS,
    FS36,
    FS38,
    IT01,
    K9,
    OB36,
    OEBS,
    PP01,
    UFM,
    BA,
    EF,
    KONT,
    SUPSTONAD,
    OMSORGSPENGER
}

enum class ArkivsaksSystem {
    GSAK,
    PSAK
}

data class Dokument(
    val tittel: String,
    val brevkode: String? = null,
    val dokumentVarianter: List<DokumentVariant> = mutableListOf()
)

data class DokumentVariant(
    val filnavn: String,
    val filtype: String,
    val fysiskDokument: String,
    val variantformat: String
)

data class Tilleggsopplysning(
    val nokkel: String,
    val verdi: String
)