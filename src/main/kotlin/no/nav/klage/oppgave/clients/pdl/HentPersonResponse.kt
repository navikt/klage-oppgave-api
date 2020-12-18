package no.nav.klage.oppgave.clients.pdl


data class HentPersonResponse(val data: HentPersonBolk?, val errors: List<PdlError>? = null)

data class HentPersonBolk(val hentPersonBolk: List<HentPersonBolkResult>?)

data class HentPersonBolkResult(
    val person: Person,
    val ident: String
) {
    data class Person(
        val navn: List<Navn>
    ) {
        data class Navn(
            val fornavn: String,
            val etternavn: String
        )
    }
}

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
