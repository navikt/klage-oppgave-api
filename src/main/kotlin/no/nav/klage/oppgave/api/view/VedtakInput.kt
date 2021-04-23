package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.kodeverk.Utfall

data class VedtakUtfallInput(val utfall: Utfall, val klagebehandlingVersjon: Long? = null)