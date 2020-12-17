package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.repositories.OppgaveKopiRepository
import no.nav.klage.oppgave.service.ElasticsearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate

@Configuration
@Import(ElasticsearchConfiguration::class)
class ElasticsearchServiceConfiguration {

    @Bean
    fun elasticsearchService(
        esTemplate: ElasticsearchRestTemplate,
        oppgaveKopiRepository: OppgaveKopiRepository
    ): ElasticsearchService {
        return ElasticsearchService(esTemplate, oppgaveKopiRepository)
    }

}