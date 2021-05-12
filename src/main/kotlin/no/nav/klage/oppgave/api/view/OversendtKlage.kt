package no.nav.klage.oppgave.api.view

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.Fagsystem
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import javax.validation.constraints.Past

@ApiModel
data class OversendtKlage(
    @ApiModelProperty(
        required = true,
        example = "OMS",
        notes = "Gyldige verdier er OMS i prod, OMS og SYK i dev"
    )
    val tema: Tema,
    @ApiModelProperty(
        required = true,
        example = "KLAGE",
        notes = "Gyldige verdier er KLAGE i både prod og dev"
    )
    val type: Type,
    @ApiModelProperty(
        required = true
    )
    val klager: OversendtKlager,
    @ApiModelProperty(
        notes = "Kan settes dersom klagen gjelder en annen enn den som har levert klagen",
        required = false
    )
    val sakenGjelder: OversendtSakenGjelder? = null,
    @ApiModelProperty(
        notes = "Fagsak brukt til journalføring. Dersom denne er tom journalfører vi på generell sak",
        required = false
    )
    val fagsak: OversendtSak? = null,
    @ApiModelProperty(
        notes = "Id som er intern for kildesystemet så vedtak fra oss knyttes riktig i kilde",
        required = true
    )
    val kildeReferanse: String,
    @ApiModelProperty(
        notes = "Id som rapporters på til DVH, bruker kildeReferanse hvis denne ikke er satt",
        required = false
    )
    val dvhReferanse: String? = null,
    @ApiModelProperty(
        notes = "Url tilbake til kildesystem for innsyn i sak",
        required = false,
        example = "https://k9-sak.adeo.no/behandling/12345678"
    )
    val innsynUrl: String?,
    @ApiModelProperty(
        notes = "Hjemler knyttet til klagen",
        required = false
    )
    val hjemler: List<HjemmelFraFoersteInstans>?,
    val avsenderSaksbehandlerIdent: String,
    val avsenderEnhet: String,
    @ApiModelProperty(
        notes = "Kan settes dersom klagen skal til en spesifikk klageinstans",
        required = false,
        example = "4219"
    )
    val oversendtEnhet: String? = null,
    @ApiModelProperty(
        notes = "Liste med relevante journalposter til klagen. Liste kan være tom.",
        required = true
    )
    val tilknyttedeJournalposter: List<OversendtDokumentReferanse>,
    @field:Past(message = "Dato for mottatt førsteinstans må være i fortiden")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val mottattFoersteinstans: LocalDate,
    val innsendtTilNav: LocalDate,
    @ApiModelProperty(
        notes = "Kan settes dersom førsteinstans ønsker å overstyre frist",
        required = false
    )
    val frist: LocalDate? = null,
    @ApiModelProperty(
        notes = "Legges ved melding ut fra KA på Kafka, brukes for filtrering",
        required = true,
        example = "FS39"
    )
    val kilde: KildeFagsystem,
    @ApiModelProperty(
        notes = "Kommentarer fra saksbehandler i førsteinstans som ikke er med i oversendelsesbrevet klager mottar",
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
        sakFagsystem = fagsak?.fagsystem?.mapFagsystem(),
        sakFagsakId = fagsak?.fagsakId,
        kildeReferanse = kildeReferanse,
        dvhReferanse = dvhReferanse,
        hjemmelListe = hjemler?.map { it.toMottakHjemmel() }?.toMutableSet(),
        avsenderSaksbehandlerident = avsenderSaksbehandlerIdent,
        avsenderEnhet = avsenderEnhet,
        oversendtKaEnhet = oversendtEnhet,
        mottakDokument = tilknyttedeJournalposter.map { it.toMottakDokument() }.toMutableSet(),
        innsendtDato = innsendtTilNav,
        mottattNavDato = mottattFoersteinstans,
        oversendtKaDato = LocalDate.now(),
        fristFraFoersteinstans = frist,
        kildesystem = kilde.mapFagsystem()
    )
}

private fun KildeFagsystem.mapFagsystem(): Fagsystem =
    when (this) {
        KildeFagsystem.AO01 -> Fagsystem.AO01
        KildeFagsystem.FS36 -> Fagsystem.FS36
        KildeFagsystem.FS39 -> Fagsystem.FS39
        KildeFagsystem.AO11 -> Fagsystem.AO11
        KildeFagsystem.BISYS -> Fagsystem.BISYS
        KildeFagsystem.FS38 -> Fagsystem.FS38
        KildeFagsystem.IT01 -> Fagsystem.IT01
        KildeFagsystem.K9 -> Fagsystem.K9
        KildeFagsystem.OB36 -> Fagsystem.OB36
        KildeFagsystem.OEBS -> Fagsystem.OEBS
        KildeFagsystem.PP01 -> Fagsystem.PP01
        KildeFagsystem.UFM -> Fagsystem.UFM
        KildeFagsystem.BA -> Fagsystem.BA
        KildeFagsystem.EF -> Fagsystem.EF
        KildeFagsystem.KONT -> Fagsystem.KONT
        KildeFagsystem.SUPSTONAD -> Fagsystem.SUPSTONAD
        KildeFagsystem.OMSORGSPENGER -> Fagsystem.OMSORGSPENGER
    }

class HjemmelFraFoersteInstans private constructor(
    @ApiModelProperty(
        required = false,
        example = "9"
    )
    val kapittel: Int?,
    @ApiModelProperty(
        required = false,
        example = "1"
    )
    val paragraf: Int?,
    @ApiModelProperty(
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
    @ApiModelProperty(
        required = true
    )
    val id: OversendtPartId,
    @ApiModelProperty(
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
    @ApiModelProperty(
        required = true
    )
    val id: OversendtPartId,
    @ApiModelProperty(
        name = "klagersProsessfullmektig",
        notes = "Kan settes dersom klager har en prosessfullmektig",
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
    @ApiModelProperty(
        required = true
    )
    val id: OversendtPartId,
    @ApiModelProperty(
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
    @ApiModelProperty(
        required = true,
        example = "PERSON / VIRKSOMHET"
    )
    val type: PartIdType,
    @ApiModelProperty(
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
    @ApiModelProperty(
        required = true,
        example = "BRUKERS_KLAGE"
    )
    val type: MottakDokumentType,
    @ApiModelProperty(
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

data class OversendtSak(
    @ApiModelProperty(
        required = true,
        example = "134132412"
    )
    val fagsakId: String,
    @ApiModelProperty(
        required = true,
        example = "FS39"
    )
    val fagsystem: KildeFagsystem
)

@ApiModel
enum class KildeFagsystem {
    FS36,
    FS39,
    AO01,
    AO11,
    BISYS,
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

