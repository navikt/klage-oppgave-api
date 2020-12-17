package no.nav.klage.oppgave.domain.oppgavekopi.helper

import no.nav.klage.oppgave.domain.oppgavekopi.Status
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class StatusConverter : AttributeConverter<Status, Long?> {

    override fun convertToDatabaseColumn(status: Status?): Long? =
        status?.let { it.statusId }

    override fun convertToEntityAttribute(statusId: Long?): Status? =
        statusId?.let { Status.of(it) }

}


