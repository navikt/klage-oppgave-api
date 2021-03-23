package no.nav.klage.oppgave.domain.vedtaksbrev

import no.nav.klage.oppgave.domain.vedtaksbrev.enums.BrevElementInputType
import no.nav.klage.oppgave.domain.vedtaksbrev.enums.BrevElementKey
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "brevelement", schema = "klage")
data class BrevElement(

    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "brev_id", insertable = false, updatable = false)
    val brevId: UUID,
    @Column(name = "key")
    @Enumerated(EnumType.STRING)
    val key: BrevElementKey,
    @Column(name = "display_text")
    var displayText: String? = null,
    @Column(name = "content")
    var content: String? = null,
    @Column(name = "element_input_type")
    @Enumerated(EnumType.STRING)
    val elementInputType: BrevElementInputType?,

)

fun BrevElement.toBrevElementView(): BrevElementView {
    return BrevElementView(
        key,
        displayText,
        content,
        elementInputType = elementInputType,
        placeholderText = key.getPlaceholderText()
    )
}

fun BrevElement.updateFields(inputElement: BrevElementView) {
    this.displayText = inputElement.displayText
    this.content = inputElement.content
}