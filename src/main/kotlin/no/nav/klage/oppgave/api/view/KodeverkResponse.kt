package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.kodeverk.*

data class KodeverkResponse(
    val eoes: List<Eoes> = Eoes.values().asList(),
    val grunn: List<Grunn> = Grunn.values().asList(),
    val kilde: List<Kilde> = Kilde.values().asList(),
    val lov: List<Lov> = Lov.values().asList(),
    val raadfoertMedLege: List<RaadfoertMedLege> = RaadfoertMedLege.values().asList(),
    val sakstype: List<Sakstype> = Sakstype.values().asList(),
    val tema: List<Tema> = Tema.values().asList(),
    val utfall: List<Utfall> = Utfall.values().asList()
)