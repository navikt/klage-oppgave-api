package no.nav.klage.oppgave.util

import brave.Tracer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.lang.String.join

@Component
class AuditLogger(
    private val tracer: Tracer,
    @Value("\${spring.application.name}")
    private val applicationName: String
) {

    companion object {
        val auditLogger = getAuditLogger()
    }

    fun logInfo(logEvent: LogEvent) {
        log(logEvent)
    }

    fun logWarning(logEvent: LogEvent) {
        log(logEvent, warning = true)
    }

    private fun log(logEvent: LogEvent, warning: Boolean = false) {
        when {
            warning -> {
                auditLogger.warn(compileLogMessage(logEvent, warning))
            }
            else -> {
                auditLogger.info(compileLogMessage(logEvent, warning))
            }
        }
    }

    private fun compileLogMessage(logEvent: LogEvent, warning: Boolean): String {
        val version = "CEF:0"
        val deviceVendor = applicationName
        val deviceProduct = "auditLog"
        val deviceVersion = "1.0"
        val deviceEventClassId = "$applicationName:accessed"
        val name = "saksbehandling av klager"
        val severity = if (warning) "WARN" else "INFO"

        val extensions = join(" ", getExtensions(logEvent))

        return join(
            "|", listOf(
                version,
                deviceVendor,
                deviceProduct,
                deviceVersion,
                deviceEventClassId,
                name,
                severity,
                extensions
            )
        )
    }

    private fun getExtensions(logEvent: LogEvent): List<String> {
        val extensions = mutableListOf<String>()
        extensions += "end=${System.currentTimeMillis()}"
        extensions += "suid=${logEvent.navIdent}"
        extensions += "duid=${logEvent.personFnr}"
        extensions += "request=${logEvent.requestURL}"
        extensions += "requestMethod=${logEvent.requestMethod}"
        extensions += "flexString1=Permit"
        extensions += "flexString1Label=Decision"
        extensions += "sproc=${tracer.currentSpan().context().traceIdString()}"
        return extensions
    }
}

data class LogEvent(
    val navIdent: String,
    val requestURL: String,
    val requestMethod: String?,
    val personFnr: String?
)