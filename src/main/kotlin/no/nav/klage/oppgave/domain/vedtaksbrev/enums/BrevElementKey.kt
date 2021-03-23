package no.nav.klage.oppgave.domain.vedtaksbrev.enums

import no.nav.klage.oppgave.domain.vedtaksbrev.BrevElement
import java.util.*

enum class BrevElementKey {
    KLAGER_NAVN {
        override fun generateDefaultBrevElement(brevId: UUID): BrevElement {
            return BrevElement(
                brevId = brevId,
                key = this,
                displayText = "Klager",
                content = "Navn Navnesen",
                elementInputType = BrevElementInputType.SINGLE_LINE_PREFILLED
            )
        }

        override fun getPlaceholderText(): String? = null
    },
    KLAGER_FNR {
        override fun generateDefaultBrevElement(brevId: UUID): BrevElement {
            return BrevElement(
                brevId = brevId,
                key = this,
                displayText = "Fødselsnummer",
                content = "010180123456",
                elementInputType = BrevElementInputType.SINGLE_LINE_PREFILLED
            )
        }

        override fun getPlaceholderText(): String? = null
    },
    SAKEN_GJELDER {
        override fun generateDefaultBrevElement(brevId: UUID): BrevElement {
            return BrevElement(
                brevId = brevId,
                key = this,
                displayText = "Saken gjelder",
                elementInputType = BrevElementInputType.SINGLE_LINE_INPUT
            )
        }

        override fun getPlaceholderText(): String =
            "Klagen din av (dato) på (enhet) sitt vedtak av (dato)"
    },
    PROBLEMSTILLING {
        override fun generateDefaultBrevElement(brevId: UUID): BrevElement {
            return BrevElement(
                brevId = brevId,
                key = this,
                displayText = "Problemstilling",
                elementInputType = BrevElementInputType.SINGLE_LINE_INPUT
            )
        }

        override fun getPlaceholderText(): String =
            "Spørsmålet er om du (tekst) fra (dato)"
    },
    VEDTAK {
        override fun generateDefaultBrevElement(brevId: UUID): BrevElement {
            return BrevElement(
                brevId = brevId,
                key = this,
                displayText = "Vedtak",
                elementInputType = BrevElementInputType.FREE_TEXT_INPUT
            )
        }

        override fun getPlaceholderText(): String =
            "Vedtaket"
    },
    BRUKERS_VEKTLEGGING {
        override fun generateDefaultBrevElement(brevId: UUID): BrevElement {
            return BrevElement(
                brevId = brevId,
                key = this,
                displayText = "I klagen din har du lagt vekt på",
                elementInputType = BrevElementInputType.FREE_TEXT_INPUT
            )
        }

        override fun getPlaceholderText(): String =
            "Skriv inn brukers anførsler"
    },
    DOKUMENT_LISTE {
        override fun generateDefaultBrevElement(brevId: UUID): BrevElement {
            return BrevElement(
                brevId = brevId,
                key = this,
                displayText = "I vurderingen vår har vi lagt vekt på disse dokumentene",
                elementInputType = BrevElementInputType.MULTIPLE_LINES_PREFILLED,
//                contentList = listOf(
//                    SaksDokument(
//                        title = "Test"
//                    ),
//                    SaksDokument(
//                        title = "Test 2"
//                    )
//                )
            )
        }

        override fun getPlaceholderText(): String? = null
    },
    VURDERING {
        override fun generateDefaultBrevElement(brevId: UUID): BrevElement {
            return BrevElement(
                brevId = brevId,
                key = this,
                displayText = "Vurderingen vår",
                elementInputType = BrevElementInputType.FREE_TEXT_INPUT
            )
        }

        override fun getPlaceholderText(): String =
            "Skriv inn vurdering"
    },
    KONKLUSJON {
        override fun generateDefaultBrevElement(brevId: UUID): BrevElement {
            return BrevElement(
                brevId = brevId,
                key = this,
                displayText = "Konklusjonen vår",
                elementInputType = BrevElementInputType.FREE_TEXT_INPUT
            )
        }

        override fun getPlaceholderText(): String =
            "Skriv inn konklusjon"
    },
    FRITEKST {
        override fun generateDefaultBrevElement(brevId: UUID): BrevElement {
            return BrevElement(
                brevId = brevId,
                key = this,
                elementInputType = BrevElementInputType.FREE_TEXT_INPUT
            )
        }

        override fun getPlaceholderText(): String? =
            null
    };

    abstract fun generateDefaultBrevElement(brevId: UUID): BrevElement
    abstract fun getPlaceholderText(): String?
}