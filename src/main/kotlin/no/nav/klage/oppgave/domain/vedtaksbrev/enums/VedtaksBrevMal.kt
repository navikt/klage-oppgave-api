package no.nav.klage.oppgave.domain.vedtaksbrev.enums

enum class VedtaksBrevMal(val elementOrder: List<BrevElementKey>) {
    FRITEKST_BREV(
      listOf(
          BrevElementKey.FRITEKST
      )
    ),
    VEDTAKS_BREV(
        listOf(
            BrevElementKey.KLAGER_NAVN,
            BrevElementKey.KLAGER_FNR,
            BrevElementKey.SAKEN_GJELDER,
            BrevElementKey.PROBLEMSTILLING,
            BrevElementKey.VEDTAK,
            BrevElementKey.BRUKERS_VEKTLEGGING,
            BrevElementKey.DOKUMENT_LISTE,
            BrevElementKey.VURDERING,
            BrevElementKey.KONKLUSJON
        )
    ),
    OMVENDT_BREV(
        listOf(
            BrevElementKey.KONKLUSJON,
            BrevElementKey.VURDERING,
            BrevElementKey.DOKUMENT_LISTE,
            BrevElementKey.BRUKERS_VEKTLEGGING,
            BrevElementKey.VEDTAK,
            BrevElementKey.PROBLEMSTILLING,
            BrevElementKey.SAKEN_GJELDER,
            BrevElementKey.KLAGER_FNR,
            BrevElementKey.KLAGER_NAVN,
        )
    )
}