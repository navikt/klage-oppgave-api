package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.klage.Mottak
import no.nav.klage.oppgave.domain.kodeverk.Kilde
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.util.*
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.constraints.Past
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

//TODO: Det er en del felter som må være nullable fra Oppgave, men som vi burde kunne kreve at er satt fra moderne løsninger. Hvordan løse det?
data class OversendtKlage(
    val uuid: UUID,
    val tema: Tema,
    val eksternReferanse: String,
    val innsynUrl: String,
    @field:Pattern(regexp = "\\d{11}", message = "Fødselsnummer er ugyldig")
    val foedselsnummer: String,
    val beskrivelse: String?,
    val avsenderSaksbehandlerIdent: String,
    val avsenderEnhet: String,
    val hjemler: List<String>,
    @field:Past(message = "Dato for mottatt førsteinstans må være i fortiden")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val mottattFoersteinstans: LocalDate?,
    val innsendtTilNav: LocalDate?,
    val sakstype: Sakstype,
    val oversendtEnhet: String,
    val oversendelsesbrevJournalpostId: String?,
    val brukersKlageJournalpostId: String?,
    val frist: LocalDate?,
    val kilde: Kilde,
    @field:MottakerAdresse
    val mottakerAdresse: Adresse?
) {

    //TODO: Orgnr/virksomhetsnr?
    //TODO: Hvis sakstype er ANKE, trenger vi da referansen til klagen?
    //TODO: Trenger vi dokumentId også, ikke bare journalpostId?
    fun toMottak() = Mottak(
        id = uuid,
        tema = tema,
        sakstype = sakstype,
        referanseId = eksternReferanse,
        innsynUrl = innsynUrl,
        foedselsnummer = foedselsnummer,
        organisasjonsnummer = null,
        virksomhetsnummer = null,
        hjemmelListe = hjemler.joinToString(separator = ","),
        beskrivelse = beskrivelse,
        avsenderSaksbehandlerident = avsenderSaksbehandlerIdent,
        avsenderEnhet = avsenderEnhet,
        oversendtKaEnhet = oversendtEnhet,
        brukersKlageJournalpostId = brukersKlageJournalpostId,
        oversendelsesbrevJournalpostId = oversendelsesbrevJournalpostId,
        innsendtDato = innsendtTilNav,
        mottattNavDato = mottattFoersteinstans,
        oversendtKaDato = LocalDate.now(),
        fristFraFoersteinstans = frist,
        kilde = kilde
    )
}

data class Adresse(
    val adressetype: Adressetype,
    val adresselinje1: String?,
    val adresselinje2: String,
    val adresselinje3: String,
    val postnummer: String?,
    val poststed: String?,
    val land: String
)

@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AdresseValidator::class])
@MustBeDocumented
annotation class MottakerAdresse(
    val message: String = "Ugyldig adresse, sjekk skjema for detaljert beskrivelse av gyldig adresse.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class AdresseValidator : ConstraintValidator<MottakerAdresse, Adresse> {
    override fun isValid(value: Adresse?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return true
        }
        if (value.adressetype == Adressetype.NORSK) {
            if (value.postnummer == null) {
                return false
            } else if (value.postnummer.toIntOrNull() == null) {
                return false
            }
            if (value.poststed == null) {
                return false
            }
        } else if (value.adressetype == Adressetype.UTENLANDSK) {
            if (value.adresselinje1 == null) {
                return false
            }
        } else {
            return false
        }
        if (value.land.length != 2) {
            return false
        }
        return true
    }

}

enum class Adressetype(val distribusjonType: String) {
    NORSK("norskPostadresse"), UTENLANDSK("utenlandskPostadresse")
}
