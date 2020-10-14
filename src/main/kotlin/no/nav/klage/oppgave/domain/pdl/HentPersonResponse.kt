package no.nav.klage.oppgave.domain.pdl


data class HentPersonResponse(val data: HentPersonBolk?, val errors: List<PdlError>? = null)

data class HentPersonBolk(val hentPersonBolk: List<Person>?)

data class Person(
    val navn: List<Navn>,
    val folkeregisteridentifikator: List<Folkeregisteridentifikator>
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
)

data class Folkeregisteridentifikator(
    val identifikasjonsnummer: String,
    val type: String,
    val status: String
)

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>?,
    val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
    val line: Int?,
    val column: Int?
)

data class PdlErrorExtension(
    val code: String?,
    val classification: String
)
