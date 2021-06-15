package no.nav.klage.oppgave.domain.klage

import org.springframework.data.domain.Persistable
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "endringslogginnslag", schema = "klage")
class Endringslogginnslag(
    @Column(name = "saksbehandlerident")
    val saksbehandlerident: String?, //subjekt?
    @Enumerated(EnumType.STRING)
    @Column(name = "kilde")
    val kilde: KildeSystem,
    @Enumerated(EnumType.STRING)
    @Column(name = "handling")
    val handling: Handling,
    @Enumerated(EnumType.STRING)
    @Column(name = "felt")
    val felt: Felt,
    @Column(name = "fraverdi")
    val fraVerdi: String?,
    @Column(name = "tilverdi")
    val tilVerdi: String?,
    @Column(name = "klagebehandling_id")
    val klagebehandlingId: UUID,
    @Id
    @JvmField
    val id: UUID = UUID.randomUUID(),
    @Column(name = "tidspunkt")
    val tidspunkt: LocalDateTime = LocalDateTime.now()
) : Persistable<UUID> {

    override fun getId(): UUID = id

    @Transient
    private var isNew = true

    override fun isNew(): Boolean {
        return isNew
    }

    @PrePersist
    @PostLoad
    fun markNotNew() {
        isNew = false
    }

    companion object {

        fun endringslogg(
            saksbehandlerident: String?,
            felt: Felt,
            fraVerdi: String?,
            tilVerdi: String?,
            klagebehandlingId: UUID,
            tidspunkt: LocalDateTime
        ): Endringslogginnslag? {
            if ((fraVerdi == null && tilVerdi == null) || fraVerdi == tilVerdi) {
                return null
            } else {
                val handling = when {
                    fraVerdi == null && tilVerdi != null -> Handling.NY
                    fraVerdi != null && tilVerdi == null -> Handling.SLETTING
                    else -> Handling.ENDRING
                }
                return Endringslogginnslag(
                    saksbehandlerident = saksbehandlerident,
                    kilde = KildeSystem.KABAL,
                    handling = handling,
                    felt = felt,
                    fraVerdi = fraVerdi,
                    tilVerdi = tilVerdi,
                    klagebehandlingId = klagebehandlingId,
                    tidspunkt = tidspunkt
                )
            }
        }
    }
}

enum class Handling {
    NY, ENDRING, SLETTING
}

enum class KildeSystem {
    KABAL, ADMIN
}

enum class Felt {
    OMGJOERINGSGRUNN, EOES, RAADGIVENDE_OVERLEGE, KVALITETSVURDERING, SEND_TILBAKEMELDING, TILBAKEMELDING, UTFALL, UTFALLETS_LOVHJEMMEL, SAKSTYPE, TEMA, HJEMMEL, DATO_PAAKLAGET_VEDTAK,
    DATO_KLAGE_INNSENDT, DATO_MOTTATT_FOERSTEINSTANS, FOERSTEINSTANS_ENHET, DATO_OVERSENDT_KA, TILDELT_SAKSBEHANDLERIDENT, TILDELT_ENHET, DATO_FRIST, AVSENDER_SAKSBEHANDLERIDENT, AVSENDER_ENHET, TILDELT, SAKSDOKUMENT, MEDUNDERSKRIVERIDENT, JOURNALPOST_I_VEDTAK, VEDTAK_SLUTTFOERT, HJEMLER_I_VEDTAK, BESTILLINGS_ID, UTSENDING_STARTET, AVSLUTTET_AV_SAKSBEHANDLER, DOKDIST_REFERANSE, AVSLUTTET, VEDTAK_DISTRIBUERT, OVERSENDT_MEDUNDERSKRIVER, OPPLASTET_I_VEDTAK
}
