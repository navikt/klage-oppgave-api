package no.nav.klage.oppgave.domain.oppgavekopi

import java.io.Serializable
import javax.persistence.Embeddable

//Denne skal egentlig ikke være her, men trengs for å lage tom constructor.. Ref https://gist.github.com/mchlstckl/4f9602b5d776878f48f0
//feltene her skulle egentlig vært val, ikke var, men da klager Intellij pga at Embeddable krever settere..
@Embeddable
data class OppgaveKopiVersjonId(var id: Long, var versjon: Int) : Serializable
