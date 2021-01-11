package no.nav.klage.oppgave.pdl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties
data class PersonDto(
    val adressebeskyttelse: List<AdressebeskyttelseDto>,
    val kjoenn: List<KjoennDto?>,
    val navn: List<NavnDto>
)