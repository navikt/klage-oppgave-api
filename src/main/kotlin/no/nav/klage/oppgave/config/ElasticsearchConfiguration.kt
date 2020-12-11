package no.nav.klage.oppgave.config

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.http.HttpHeaders
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class ElasticsearchConfiguration(
    @Value("\${aiven.es.host}") val host: String,
    @Value("\${aiven.es.port}") val port: String,
    @Value("\${aiven.es.username}") val username: String,
    @Value("\${aiven.es.username}") val password: String
) : AbstractElasticsearchConfiguration() {

    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {

        val clientConfiguration: ClientConfiguration = ClientConfiguration.builder()
            .connectedTo("$host:$port")
            .withConnectTimeout(Duration.ofSeconds(5))
            .withSocketTimeout(Duration.ofSeconds(3))
            .withBasicAuth(username, password)
            .withHeaders {
                val headers = HttpHeaders()
                headers.add("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                headers
            }
            .build();
        return RestClients.create(clientConfiguration).rest();
    }

}