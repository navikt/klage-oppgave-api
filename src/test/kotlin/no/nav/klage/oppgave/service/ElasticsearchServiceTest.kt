package no.nav.klage.oppgave.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
class ElasticsearchServiceTest {

    @Container
    private val ES_CONTAINER: ElasticsearchContainer =
        ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:6.8.13")
    //.withEnv("discovery.type", "single-node")

    @Test
    fun test() {
        assertThat(ES_CONTAINER.isRunning()).isTrue()
    }
}