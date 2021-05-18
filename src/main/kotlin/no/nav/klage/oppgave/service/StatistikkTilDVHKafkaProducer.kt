package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.kafka.KlageStatistikkTilDVH
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class StatistikkTilDVHKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, KlageStatistikkTilDVH>
) {
    @Value("\${DVH_STATISTIKK_TOPIC}")
    lateinit var topic: String

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun sendStatistikkTilDVH(statistikk: KlageStatistikkTilDVH) {
        logger.debug("Sending to Kafka topic: {}", topic)
        secureLogger.debug("Sending to Kafka topic: {}\nKlageStatistikkTilDVH: {}", topic, statistikk)
        runCatching {
            kafkaTemplate.send(topic, statistikk)
            logger.debug("KlageStatistikkTilDVH sent to Kafka.")
        }.onFailure {
            val errorMessage = "Could not send KlageStatistikkTilDVH to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send KlageStatistikkTilDVH to Kafka", it)
            throw RuntimeException(errorMessage)
        }
    }
}