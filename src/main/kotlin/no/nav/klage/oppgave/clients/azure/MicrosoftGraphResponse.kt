package no.nav.klage.oppgave.clients.azure

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class MicrosoftGraphIdentResponse(val onPremisesSamAccountName: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MicrosoftGraphNameResponse(val value: List<Value>?) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Value(val onPremisesSamAccountName: String?, val displayName: String?)
}