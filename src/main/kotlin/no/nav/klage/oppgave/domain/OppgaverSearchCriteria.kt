package no.nav.klage.oppgave.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class OppgaverSearchCriteria(
    val typer: List<String> = emptyList(),
    val temaer: List<String> = emptyList(),
    val hjemler: List<String> = emptyList(),
    val statuskategori: Statuskategori = Statuskategori.AAPEN,

    val opprettetFom: LocalDateTime? = null,
    val opprettetTom: LocalDateTime? = null,
    val ferdigstiltFom: LocalDateTime? = null,
    val ferdigstiltTom: LocalDateTime? = null,
    val fristFom: LocalDate? = null,
    val fristTom: LocalDate? = null,

    val order: Order? = null,
    val offset: Int = 0,
    val limit: Int = 10,
    val erTildeltSaksbehandler: Boolean? = null,
    val saksbehandler: String? = null,
    val enhetsnr: String? = null,
    val projection: Projection? = null
) {
    enum class Order {
        ASC, DESC
    }

    enum class Statuskategori {
        AAPEN, AVSLUTTET
    }


    enum class Projection {
        UTVIDET
    }

    fun isProjectionUtvidet(): Boolean = Projection.UTVIDET == projection

}