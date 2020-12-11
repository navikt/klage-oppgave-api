package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.config.ElasticsearchServiceConfiguration
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import org.apache.http.util.EntityUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.elasticsearch.UncategorizedElasticsearchException
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.lang.Thread.sleep


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    initializers = [ElasticsearchServiceTest.Companion.Initializer::class],
    classes = [ElasticsearchServiceConfiguration::class]
)
class ElasticsearchServiceTest {

    companion object {
        @Container
        @JvmField
        val ES_CONTAINER: ElasticsearchContainer =
            ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.9.3")

        class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {

                TestPropertyValues.of(
                    "aiven.es.host=${ES_CONTAINER.host}",
                    "aiven.es.port=${ES_CONTAINER.firstMappedPort}",
                    "aiven.es.username=elastic",
                    "aiven.es.password=changeme",
                ).applyTo(configurableApplicationContext.environment)
            }
        }
    }

    @Autowired
    lateinit var service: ElasticsearchService

    @Autowired
    lateinit var esTemplate: ElasticsearchRestTemplate

    @Autowired
    lateinit var client: RestHighLevelClient

    @Test
    @Order(1)
    fun `es is running`() {
        assertThat(ES_CONTAINER.isRunning).isTrue
    }

    @Test
    @Order(2)
    fun `index has been created by service`() {

        val indexOps = esTemplate.indexOps(IndexCoordinates.of("oppgavekopier"))
        assertThat(indexOps.exists()).isTrue()
    }

    @Test
    @Order(3)
    fun `oppgave can be saved and retrieved`() {

        val oppgave = EsOppgave()
        oppgave.id = 1001L
        oppgave.versjon = 1L
        oppgave.journalpostId = "hei"
        esTemplate.save(oppgave)

        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsOppgave> = esTemplate.search(query, EsOppgave::class.java)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.journalpostId).isEqualTo("hei")
    }

    @Test
    @Order(4)
    fun `oppgave can be saved twice without creating a duplicate`() {

        val oppgave = EsOppgave()
        oppgave.id = 2001L
        oppgave.versjon = 1L
        oppgave.journalpostId = "hei"
        esTemplate.save(oppgave)

        oppgave.versjon = 2L
        oppgave.journalpostId = "hallo"
        esTemplate.save(oppgave)
        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.idsQuery().addIds("2001"))
            .build()
        val searchHits: SearchHits<EsOppgave> = esTemplate.search(query, EsOppgave::class.java)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.journalpostId).isEqualTo("hallo")
    }

    @Test
    @Order(5)
    fun `saving an earlier version of oppgave causes a conflict`() {

        val oppgave = EsOppgave()
        oppgave.id = 3001L
        oppgave.versjon = 2L
        oppgave.journalpostId = "hei"
        esTemplate.save(oppgave)

        oppgave.versjon = 1L
        oppgave.journalpostId = "hallo"
        assertThatThrownBy {
            esTemplate.save(oppgave)
        }.isInstanceOf(UncategorizedElasticsearchException::class.java)
            .hasRootCauseInstanceOf(ElasticsearchStatusException::class.java)
            .hasMessageContaining("type=version_conflict_engine_exception")

        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.idsQuery().addIds("3001"))
            .build()
        val searchHits: SearchHits<EsOppgave> = esTemplate.search(query, EsOppgave::class.java)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.journalpostId).isEqualTo("hei")
    }

    @Test
    @Order(6)
    @Disabled("kan brukes for å generere settings og mapping, for så å lagre som fil")
    fun `denne vil printe ut settings og mapping`() {
        val mappingResponse = client.lowLevelClient.performRequest(Request("GET", "/_all/_mapping"))
        val mapping: String = EntityUtils.toString(mappingResponse.entity)
        println(mapping)
        val settingsResponse = client.lowLevelClient.performRequest(Request("GET", "/_all/_settings"))
        val settings: String = EntityUtils.toString(settingsResponse.entity)
        println(settings)
    }
}



