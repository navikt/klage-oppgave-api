package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.config.ElasticsearchServiceConfiguration
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import no.nav.klage.oppgave.domain.elasticsearch.Prioritet
import no.nav.klage.oppgave.domain.elasticsearch.Status
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
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
import java.time.LocalDate
import java.time.LocalDateTime


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

        val oppgave = oppgaveWith(
            id = 1001L,
            versjon = 1L,
            beskrivelse = "hei"
        )
        esTemplate.save(oppgave)

        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsOppgave> = esTemplate.search(query, EsOppgave::class.java)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.beskrivelse).isEqualTo("hei")
    }

    @Test
    @Order(4)
    fun `oppgave can be saved twice without creating a duplicate`() {

        var oppgave = oppgaveWith(
            id = 2001L,
            versjon = 1L,
            beskrivelse = "hei"
        )
        esTemplate.save(oppgave)

        oppgave = oppgaveWith(
            id = 2001L,
            versjon = 2L,
            beskrivelse = "hallo"
        )
        esTemplate.save(oppgave)
        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.idsQuery().addIds("2001"))
            .build()
        val searchHits: SearchHits<EsOppgave> = esTemplate.search(query, EsOppgave::class.java)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.beskrivelse).isEqualTo("hallo")
    }

    @Test
    @Order(5)
    fun `saving an earlier version of oppgave causes a conflict`() {

        var oppgave = oppgaveWith(
            id = 3001L,
            versjon = 2L,
            beskrivelse = "hei"
        )
        esTemplate.save(oppgave)

        oppgave = oppgaveWith(
            id = 3001L,
            versjon = 1L,
            beskrivelse = "hallo"
        )
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
        assertThat(searchHits.searchHits.first().content.beskrivelse).isEqualTo("hei")
    }

    private fun oppgaveWith(id: Long, versjon: Long, beskrivelse: String): EsOppgave {
        return EsOppgave(
            id = id,
            versjon = versjon,
            tema = "tema",
            status = Status.OPPRETTET,
            tildeltEnhetsnr = "4219",
            oppgavetype = "KLAGE",
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = LocalDate.now(),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.now(),
            beskrivelse = beskrivelse
        )
    }
}



