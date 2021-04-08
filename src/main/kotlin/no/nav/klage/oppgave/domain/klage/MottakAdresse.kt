package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.api.view.Adressetype
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "mottak_adresse", schema = "klage")
class MottakAdresse(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "adressetype")
    var adressetype: Adressetype,
    @Column(name = "adresselinje1")
    var adresselinje1: String?,
    @Column(name = "adresselinje2")
    var adresselinje2: String?,
    @Column(name = "adresselinje3")
    var adresselinje3: String?,
    @Column(name = "postnummer")
    var postnummer: String?,
    @Column(name = "poststed")
    var poststed: String?,
    @Column(name = "land")
    var land: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MottakAdresse

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
