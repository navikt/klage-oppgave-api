package no.nav.klage.oppgave.api.view

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import javax.validation.constraints.Past

@Schema
data class OversendtKlage(
    @Schema(
        required = true,
        example = "OMS"
    )
    val tema: Tema,
    @Schema(
        required = true,
        example = "KLAGE"
    )
    val type: Type,
    @Schema(
        required = true
    )
    val klager: OversendtKlager,
    @Schema(
        description = "Kan settes dersom klagen gjelder en annen enn den som har levert klagen",
        required = false
    )
    val sakenGjelder: OversendtSakenGjelder? = null,
    @Schema(
        description = "Saksnummer brukt til journalføring. Vi oppretter sak dersom denne er tom",
        required = false
    )
    val sakReferanse: String? = null,
    @Schema(
        description = "Id som er intern for kildesystemet så vedtak fra oss knyttes riktig i kilde",
        required = true
    )
    val kildeReferanse: String,
    @Schema(
        description = "Id som rapporters på til DVH, bruker kildeReferanse hvis denne ikke er satt",
        required = false
    )
    val dvhReferanse: String? = null,
    @Schema(
        description = "Url tilbake til kildesystem for innsyn i sak",
        required = false,
        example = "https://k9-sak.adeo.no/behandling/12345678"
    )
    val innsynUrl: String?,
    @Schema(
        description = "Hjemler knyttet til klagen",
        required = true
    )
    val hjemler: List<HjemmelFraFoersteInstans>,
    val avsenderSaksbehandlerIdent: String,
    val avsenderEnhet: String,
    @Schema(
        description = "Kan settes dersom klagen skal til en spesifikk klageinstans",
        required = false,
        example = "4219"
    )
    val oversendtEnhet: String? = null,
    @Schema(
        description = "Liste med relevante journalposter til klagen. Liste kan være tom.",
        required = true
    )
    val tilknyttedeJournalposter: List<OversendtDokumentReferanse>,
    @field:Past(message = "Dato for mottatt førsteinstans må være i fortiden")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val mottattFoersteinstans: LocalDate,
    val innsendtTilNav: LocalDate,
    @Schema(
        description = "Kan settes dersom førsteinstans ønsker å overstyre frist",
        required = false
    )
    val frist: LocalDate? = null,
    @Schema(
        description = "Legges ved melding ut fra KA på Kafka, brukes for filtrering",
        required = true,
        example = "K9-sak"
    )
    val kilde: String,
    @Schema(
        description = "Kommentarer fra saksbehandler i førsteinstans som ikke er med i oversendelsesbrevet klager mottar",
        required = false
    )
    val kommentar: String? = null
) {
    fun toMottak() = Mottak(
        tema = tema,
        type = type,
        klager = klager.toKlagepart(),
        sakenGjelder = sakenGjelder?.toSakenGjelder(),
        innsynUrl = innsynUrl,
        sakReferanse = sakReferanse,
        kildeReferanse = kildeReferanse,
        dvhReferanse = dvhReferanse,
        hjemmelListe = hjemler.map { it.toMottakHjemmel() }.toMutableSet(),
        avsenderSaksbehandlerident = avsenderSaksbehandlerIdent,
        avsenderEnhet = avsenderEnhet,
        oversendtKaEnhet = oversendtEnhet,
        mottakDokument = tilknyttedeJournalposter.map { it.toMottakDokument() }.toMutableSet(),
        innsendtDato = innsendtTilNav,
        mottattNavDato = mottattFoersteinstans,
        oversendtKaDato = LocalDate.now(),
        fristFraFoersteinstans = frist,
        kilde = kilde
    )
}

class HjemmelFraFoersteInstans private constructor(
    @Schema(
        required = false,
        example = "9"
    )
    val kapittel: Int?,
    @Schema(
        required = false,
        example = "1"
    )
    val paragraf: Int?,
    @Schema(
        required = true
    )
    val lov: Lov
) {
    constructor(lov: Lov) : this(null, null, lov)
    constructor(lov: Lov, kapittel: Int) : this(kapittel, null, lov)
    constructor(lov: Lov, kapittel: Int, paragraf: Int) : this(kapittel, paragraf, lov)

    override fun toString(): String {
        if (kapittel != null && paragraf != null) {
            return "$lov $kapittel-$paragraf"
        } else if (kapittel != null) {
            return "$lov $kapittel"
        } else {
            return "$lov"
        }
    }

    fun toMottakHjemmel() = MottakHjemmel(lov = lov, kapittel = kapittel, paragraf = paragraf)
}

enum class Lov {
    FOLKETRYGDLOVEN, FORVALTNINGSLOVEN
}

data class OversendtSakenGjelder(
    @Schema(
        required = true
    )
    val id: OversendtPartId,
    @Schema(
        required = true,
        example = "true"
    )
    val skalMottaKopi: Boolean
) {
    fun toSakenGjelder() = SakenGjelder(
        partId = id.toPartId(),
        skalMottaKopi = skalMottaKopi
    )
}

data class OversendtKlager(
    @Schema(
        required = true
    )
    val id: OversendtPartId,
    @Schema(
        name = "klagersProsessfullmektig",
        description = "Kan settes dersom klager har en prosessfullmektig",
        required = false
    )
    val klagersProsessfullmektig: OversendtProsessfullmektig? = null
) {
    fun toKlagepart() = Klager(
        partId = id.toPartId(),
        prosessfullmektig = klagersProsessfullmektig?.toProsessfullmektig()
    )
}

data class OversendtProsessfullmektig(
    @Schema(
        required = true
    )
    val id: OversendtPartId,
    @Schema(
        required = true,
        example = "true"
    )
    val skalKlagerMottaKopi: Boolean
) {
    fun toProsessfullmektig() = Prosessfullmektig(
        partId = id.toPartId(),
        skalKlagerMottaKopi = skalKlagerMottaKopi
    )
}

data class OversendtPartId(
    @Schema(
        required = true,
        example = "PERSON / VIRKSOMHET"
    )
    val type: PartIdType,
    @Schema(
        required = true,
        example = "12345678910"
    )
    val verdi: String
) {
    fun toPartId() = PartId(
        type = type,
        value = verdi
    )
}

data class OversendtDokumentReferanse(
    @Schema(
        required = true,
        example = "BRUKERS_KLAGE"
    )
    val type: MottakDokumentType,
    @Schema(
        required = true,
        example = "830498203"
    )
    val journalpostId: String
) {
    fun toMottakDokument() = MottakDokument(
        type = type,
        journalpostId = journalpostId
    )
}
