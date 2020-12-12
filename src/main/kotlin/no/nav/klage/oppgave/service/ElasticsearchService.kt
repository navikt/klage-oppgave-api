package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import no.nav.klage.oppgave.domain.view.TYPE_ANKE
import no.nav.klage.oppgave.domain.view.TYPE_KLAGE
import no.nav.klage.oppgave.util.getLogger
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.document.Document
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.Query
import java.time.format.DateTimeFormatter

class ElasticsearchService(val esTemplate: ElasticsearchRestTemplate) :
    ApplicationListener<ContextRefreshedEvent> {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val indexOps = esTemplate.indexOps(IndexCoordinates.of("oppgavekopier"))
        if (!indexOps.exists()) {
            indexOps.create(settings)
            indexOps.putMapping(mapping)
        }
    }

    fun oppgaveSearch(criteria: OppgaverSearchCriteria): List<EsOppgave> {
        val query: Query = NativeSearchQueryBuilder()
            //.withPageable(toPageable(criteria))
            .withSort(SortBuilders.fieldSort("fristFerdigstillelse").order(mapOrder(criteria.order)))
            .withQuery(criteria.toEsQuery())
            .build()
        val searchHits: SearchHits<EsOppgave> = esTemplate.search(query, EsOppgave::class.java)
        println("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits.searchHits.map { it.content }
    }

    private fun mapOrder(order: OppgaverSearchCriteria.Order?): SortOrder {
        return order.let {
            when {
                it == null -> SortOrder.ASC
                it == OppgaverSearchCriteria.Order.ASC -> SortOrder.ASC
                it == OppgaverSearchCriteria.Order.DESC -> SortOrder.DESC
                else -> SortOrder.ASC
            }
        }
    }

    private fun toPageable(criteria: OppgaverSearchCriteria): Pageable {
        val page: Int = (criteria.offset / criteria.limit)
        val size: Int = criteria.limit
        return PageRequest.of(page, size)
    }

    private fun OppgaverSearchCriteria.toEsQuery(): QueryBuilder {

        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        logger.debug("Search criteria: {}", this)
        baseQuery.must(QueryBuilders.termQuery("statuskategori", statuskategori))

        enhetsnr?.let {
            baseQuery.must(QueryBuilders.termQuery("tildeltEnhetsnr", enhetsnr))
        }

        val innerQueryBehandlingtype = QueryBuilders.boolQuery()
        baseQuery.must(innerQueryBehandlingtype)
        if (typer.isNotEmpty()) {
            typer.forEach {
                innerQueryBehandlingtype.should(QueryBuilders.termQuery("behandlingstype", it))
            }
        } else {
            innerQueryBehandlingtype.should(QueryBuilders.termQuery("behandlingstype", TYPE_KLAGE))
            innerQueryBehandlingtype.should(QueryBuilders.termQuery("behandlingstype", TYPE_ANKE))
        }

        val innerQueryTema = QueryBuilders.boolQuery()
        baseQuery.must(innerQueryTema)
        ytelser.forEach {
            innerQueryTema.should(QueryBuilders.termQuery("tema", it))
        }

        erTildeltSaksbehandler?.let {
            if (erTildeltSaksbehandler) {
                baseQuery.must(QueryBuilders.existsQuery("tilordnetRessurs"))
            } else {
                baseQuery.mustNot(QueryBuilders.existsQuery("tilordnetRessurs"))
            }
        }
        saksbehandler?.let {
            baseQuery.must(QueryBuilders.termQuery("tilordnetRessurs", saksbehandler))
        }

        opprettetFom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("opprettetTidspunkt").gte(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it))
            )
        }
        opprettetTom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("opprettetTidspunkt").lte(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it))
            )
        }
        ferdigstiltFom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("ferdigstiltTidspunkt").gte(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it))
            )
        }
        ferdigstiltTom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("ferdigstiltTidspunkt").lte(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it))
            )
        }
        fristFom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("fristFerdigstillelse").gte(DateTimeFormatter.ISO_LOCAL_DATE.format(it))
            )
        }
        fristTom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("fristFerdigstillelse").lte(DateTimeFormatter.ISO_LOCAL_DATE.format(it))
            )
        }

        if (hjemler.isNotEmpty()) {
            val innerQueryHjemler = QueryBuilders.boolQuery()
            baseQuery.must(innerQueryHjemler)
            hjemler.forEach {
                innerQueryHjemler.should(QueryBuilders.termQuery("hjemler", it))
            }
        }

        logger.info("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    val settings = Document.parse(
        """
        {
          "index": {
            "refresh_interval": "1s",
            "number_of_shards": "3",
            "number_of_replicas": "2"
          }
        }        
        """.trimIndent()
    )

    val mapping = Document.parse(
        """
        {
          "properties": {
            "status": {
              "type": "keyword"
            },
            "tildeltEnhetsnr": {
              "type": "keyword"
            },
            "opprettetAvEnhetsnr": {
              "type": "keyword"
            },
            "endretAvEnhetsnr": {
              "type": "keyword"
            },
            "tema": {
              "type": "keyword"
            },
            "temagruppe": {
              "type": "keyword"
            },
            "behandlingstema": {
              "type": "keyword"
            },
            "oppgavetype": {
              "type": "keyword"
            },
            "behandlingstype": {
              "type": "keyword"
            },
            "prioritet": {
              "type": "keyword"
            },
            "tilordnetRessurs": {
              "type": "keyword"
            },
            "fristFerdigstillelse": {
              "type": "date",
              "format": "date"
            },
            "aktivDato": {
              "type": "date",
              "format": "date"
            },
            "opprettetAv": {
              "type": "keyword"
            },
            "endretAv": {
              "type": "keyword"
            },
            "opprettetTidspunkt": {
              "type": "date",
              "format": "date_time"
            },
            "endretTidspunkt": {
              "type": "date",
              "format": "date_time"
            },
            "ferdigstiltTidspunkt": {
              "type": "date",
              "format": "date_time"
            },
            "aktoerId": {
              "type": "keyword"
            },
            "fnr": {
              "type": "keyword"
            },
            "statuskategori": {
              "type": "keyword"
            }
          }
        }
        """.trimIndent()
    )


}


