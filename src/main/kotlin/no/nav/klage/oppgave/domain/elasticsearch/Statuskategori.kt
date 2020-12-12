package no.nav.klage.oppgave.domain.elasticsearch

enum class Statuskategori {
    AAPEN,
    AVSLUTTET;

    fun statuserForKategori(kategori: Statuskategori): List<Status> {
        return when (kategori) {
            AAPEN -> aapen()
            AVSLUTTET -> avsluttet()
        }
    }

    fun avsluttet(): List<Status> {
        return listOf(Status.FERDIGSTILT, Status.FEILREGISTRERT)
    }

    fun aapen(): List<Status> {
        return listOf(Status.OPPRETTET, Status.AAPNET, Status.UNDER_BEHANDLING)
    }
}
