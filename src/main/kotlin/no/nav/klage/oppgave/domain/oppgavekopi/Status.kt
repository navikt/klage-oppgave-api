package no.nav.klage.oppgave.domain.oppgavekopi

enum class Status(val statusId: Long) {

    OPPRETTET(1),
    AAPNET(2),
    UNDER_BEHANDLING(3),
    FERDIGSTILT(4),
    FEILREGISTRERT(5);

    companion object {
        fun kategoriForStatus(status: Status): Statuskategori {
            return when (status) {
                AAPNET, OPPRETTET, UNDER_BEHANDLING -> Statuskategori.AAPEN
                FEILREGISTRERT, FERDIGSTILT -> Statuskategori.AVSLUTTET
            }
        }

        fun fraStatusId(statusId: Long): Status =
            when (statusId) {
                1L -> OPPRETTET
                2L -> AAPNET
                3L -> UNDER_BEHANDLING
                4L -> FERDIGSTILT
                5L -> FEILREGISTRERT
                else -> throw RuntimeException("Unknown status")
            }
    }

    fun kategoriForStatus(): Statuskategori {
        return kategoriForStatus(this)
    }
}
