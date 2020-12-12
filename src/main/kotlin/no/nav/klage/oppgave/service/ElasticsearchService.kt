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
            "aktivDato": {
              "type": "date"
            },
            "beskrivelse": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            },
            "egenAnsatt": {
              "type": "boolean"
            },
            "fristFerdigstillelse": {
              "type": "date"
            },
            "id": {
              "type": "long"
            },
            "oppgavetype": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            },
            "opprettetAv": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            },
            "opprettetTidspunkt": {
              "type": "date"
            },
            "prioritet": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            },
            "status": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            },
            "statuskategori": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            },
            "tema": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            },
            "tildeltEnhetsnr": {
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