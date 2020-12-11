package no.nav.klage.oppgave.service

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.document.Document
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates

class ElasticsearchService(val esTemplate: ElasticsearchRestTemplate) :
    ApplicationListener<ContextRefreshedEvent> {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val indexOps = esTemplate.indexOps(IndexCoordinates.of("oppgavekopier"))
        if (!indexOps.exists()) {
            indexOps.create(settings)
            indexOps.putMapping(mapping)
        }
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
            "_class": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            },
            "id": {
              "type": "long"
            },
            "journalpostId": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            },
            "versjon": {
              "type": "long"
            }
          }
        }       
        """.trimIndent()
    )


}