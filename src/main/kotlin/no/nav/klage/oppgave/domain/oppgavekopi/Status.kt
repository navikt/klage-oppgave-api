package no.nav.klage.oppgave.domain.oppgavekopi

enum class Status(val statusId: Long) {

    OPPRETTET(1),
    AAPNET(2),
    UNDER_BEHANDLING(3),
    FERDIGSTILT(4),
    FEILREGISTRERT(5);

    companion object {

        fun of(statusId: Long): Status {
            return values().firstOrNull { it.statusId === statusId }
                ?: throw IllegalArgumentException("No status with ${statusId} exists")
        }

        fun kategoriForStatus(status: Status): Statuskategori {
            return when (status) {
                AAPNET, OPPRETTET, UNDER_BEHANDLING -> Statuskategori.AAPEN
                FEILREGISTRERT, FERDIGSTILT -> Statuskategori.AVSLUTTET
            }
        }
    }

    fun kategoriForStatus(): Statuskategori {
        return kategoriForStatus(this)
    }
}
