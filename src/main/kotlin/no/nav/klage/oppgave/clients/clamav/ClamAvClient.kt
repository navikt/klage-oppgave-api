package no.nav.klage.oppgave.clients.clamav

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ClamAvClient(private val clamAvWebClient: WebClient) {

    private val logger = LoggerFactory.getLogger(ClamAvClient::class.java)

    fun scan(file: ByteArray): Boolean {
        logger.debug("Scanning document")
        val response = try {
            clamAvWebClient.put()
                .bodyValue(file)
                .retrieve()
                .bodyToMono<List<ScanResult>>()
                .block()
        } catch(ex: Throwable) {
            logger.warn("Error from clamAV", ex)
            listOf(ScanResult("Unknown", ClamAvResult.ERROR))
        }

        if(response.size != 1) {
            logger.warn("Wrong size response from virus scan.")
            return false
        }

        val (filename, result) = response[0]
        logger.debug("$filename ${result.name}")
        return when(result) {
            ClamAvResult.OK -> true
            ClamAvResult.FOUND -> {
                logger.warn("$filename has virus")
                false
            }
            ClamAvResult.ERROR -> {
                logger.warn("Error from virus scan on file $filename")
                false
            }
        }
    }
}