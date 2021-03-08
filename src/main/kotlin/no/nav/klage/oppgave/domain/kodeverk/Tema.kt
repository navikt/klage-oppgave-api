package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Tema(val id: String, val navn: String, val beskrivelse: String) {

    AAP("AAP", "Arbeidsavklaringspenger", "Arbeidsavklaringspenger"),
    AAR("AAR", "Aa-registeret", "Aa-registeret"),
    AGR("AGR", "Ajourhold - Grunnopplysninger", "Ajourhold - Grunnopplysninger"),
    BAR("BAR", "Barnetrygd", "Barnetrygd"),
    BID("BID", "Bidrag", "Bidrag"),
    BIL("BIL", "Bil", "Bil"),
    DAG("DAG", "Dagpenger", "Dagpenger"),
    ENF("ENF", "Enslig forsørger", "Enslig forsørger"),
    ERS("ERS", "Erstatning", "Erstatning"),
    FAR("FAR", "Farskap", "Farskap"),
    FEI("FEI", "Feilutbetaling", "Feilutbetaling"),
    FOR("FOR", "Foreldre- og svangerskapspenger", "Foreldre- og svangerskapspenger"),
    FOS("FOS", "Forsikring", "Forsikring"),
    FRI(
        "FRI",
        "Kompensasjon for selvstendig næringsdrivende/frilansere",
        "Kompensasjon for selvstendig næringsdrivende/frilansere"
    ),
    FUL("FUL", "Fullmakt", "Fullmakt"),
    GEN("GEN", "Generell", "Generell"),
    GRA("GRA", "Gravferdsstønad", "Gravferdsstønad"),
    GRU("GRU", "Grunn- og hjelpestønad", "Grunn- og hjelpestønad"),
    HEL("HEL", "Helsetjenester og ortopediske hjelpemidler", "Helsetjenester og ortopediske hjelpemidler"),
    HJE("HJE", "Hjelpemidler", "Hjelpemidler"),
    IAR("IAR", "Inkluderende arbeidsliv", "Inkluderende arbeidsliv"),
    IND("IND", "Tiltakspenger", "Tiltakspenger"),
    KON("KON", "Kontantstøtte", "Kontantstøtte"),
    KTR("KTR", "Kontroll", "Kontroll"),
    MED("MED", "Medlemskap", "Medlemskap"),
    MOB("MOB", "Mobilitetsfremmende stønad", "Mobilitetsfremmende stønad"),
    OMS("OMS", "Omsorgspenger, pleiepenger og opplæringspenger", "Omsorgspenger, pleiepenger og opplæringspenger"),
    OPA("OPA", "Oppfølging - Arbeidsgiver", "Oppfølging - Arbeidsgiver"),
    OPP("OPP", "Oppfølging", "Oppfølging"),
    PEN("PEN", "Pensjon", "Pensjon"),
    PER("PER", "Permittering og masseoppsigelser", "Permittering og masseoppsigelser"),
    REH("REH", "Rehabilitering", "Rehabilitering"),
    REK("REK", "Rekruttering og stilling", "Rekruttering og stilling"),
    RPO("RPO", "Retting av personopplysninger", "Retting av personopplysninger"),
    RVE("RVE", "Rettferdsvederlag", "Rettferdsvederlag"),
    SAA("SAA", "Sanksjon - Arbeidsgiver", "Sanksjon - Arbeidsgiver"),
    SAK("SAK", "Saksomkostninger", "Saksomkostninger"),
    SAP("SAP", "Sanksjon - Person", "Sanksjon - Person"),
    SER("SER", "Serviceklager", "Serviceklager"),
    SIK("SIK", "Sikkerhetstiltak", "Sikkerhetstiltak"),
    STO("STO", "Regnskap/utbetaling", "Regnskap/utbetaling"),
    SUP("SUP", "Supplerende stønad", "	Supplerende stønad"),
    SYK("SYK", "Sykepenger", "Sykepenger"),
    SYM("SYM", "Sykmeldinger", "Sykmeldinger"),
    TIL("TIL", "Tiltak", "Tiltak"),
    TRK("TRK", "Trekkhåndtering", "Trekkhåndtering"),
    TRY("TRY", "Trygdeavgift", "Trygdeavgift"),
    TSO("TSO", "Tilleggsstønad", "Tilleggsstønad"),
    TSR("TSR", "Tilleggsstønad arbeidssøkere", "Tilleggsstønad arbeidssøkere"),
    UFM("UFM", "Unntak fra medlemskap", "Unntak fra medlemskap"),
    UFO("UFO", "Uføretrygd", "Uføretrygd"),
    UKJ("UKJ", "Ukjent", "Ukjent"),
    VEN("VEN", "Ventelønn", "Ventelønn"),
    YRA("YRA", "Yrkesrettet attføring", "Yrkesrettet attføring"),
    YRK("YRK", "Yrkesskade / Menerstatning", "Yrkesskade / Menerstatning"),
    GOS("GOS", "Gosys", "Er ikke egentlig et tema, men returneres fra Axsys likevel");

    override fun toString(): String {
        return "Tema(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: String): Tema {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Tema with ${id} exists")
        }

        fun fromNavn(navn: String): Tema {
            return values().firstOrNull { it.navn == navn }
                ?: throw IllegalArgumentException("No Tema with ${navn} exists")
        }
    }
}

@Converter
class TemaConverter : AttributeConverter<Tema, String?> {

    override fun convertToDatabaseColumn(entity: Tema?): String? =
        entity?.let { it.id }

    override fun convertToEntityAttribute(id: String?): Tema? =
        id?.let { Tema.of(it) }
}