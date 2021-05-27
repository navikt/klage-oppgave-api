package no.nav.klage.oppgave.config

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.binder.MeterBinder
import no.nav.klage.oppgave.service.ElasticsearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.atomic.AtomicLong


@Configuration
class FunksjonelleGaugesConfiguration(private val elasticsearchService: ElasticsearchService) {

    @Bean
    fun klagebehandlingMetricsReporter(): KlagebehandlingMetricsReporter =
        KlagebehandlingMetricsReporter(elasticsearchService)

}

class KlagebehandlingMetricsReporter(private val elasticsearchService: ElasticsearchService) : MeterBinder {

    private val antallIkkeTildelt = AtomicLong(elasticsearchService.countTildelt())
    private val antallTildelt = AtomicLong(elasticsearchService.countIkkeTildelt())
    private val antallSendtTilMedunderskriver = AtomicLong(elasticsearchService.countSendtTilMedunderskriver())
    private val antallAvsluttetAvSaksbehandler = AtomicLong(elasticsearchService.countAvsluttetAvMedunderskriver())
    private val antallAvsluttet = AtomicLong(elasticsearchService.countAvsluttet())
    private val antallSaksdokumenterMedian = AtomicLong(elasticsearchService.countAntallSaksdokumenterMedian())

    private lateinit var antallSaksdokumenterMultiGauge: MultiGauge

    override fun bindTo(registry: MeterRegistry) {
        Gauge.builder("funksjonell.ikketildelt") { antallIkkeTildelt }
            .register(registry)
        Gauge.builder("funksjonell.tildelt") { antallTildelt }
            .register(registry)
        Gauge.builder("funksjonell.sendttilmedunderskriver") { antallSendtTilMedunderskriver }
            .register(registry)
        Gauge.builder("funksjonell.avsluttetavsaksbehandler") { antallAvsluttetAvSaksbehandler }
            .register(registry)
        Gauge.builder("funksjonell.avsluttet") { antallAvsluttet }
            .register(registry)
        Gauge.builder("funksjonell.antallsaksdokumenter.median") { antallSaksdokumenterMedian }
            .register(registry)
        antallSaksdokumenterMultiGauge = MultiGauge.builder("funksjonell.antallsaksdokumenterperkb").register(registry)

    }

    @Scheduled(fixedDelay = 5000)
    fun measure() {
        //Her kan man jo evt velge å gjøre en spørring i stedet for mange..
        antallIkkeTildelt.set(elasticsearchService.countIkkeTildelt())
        antallTildelt.set(elasticsearchService.countIkkeTildelt())
        antallSendtTilMedunderskriver.set(elasticsearchService.countSendtTilMedunderskriver())
        antallAvsluttetAvSaksbehandler.set(elasticsearchService.countAvsluttetAvMedunderskriver())
        antallAvsluttet.set(elasticsearchService.countAvsluttet())
        antallSaksdokumenterMedian.set(elasticsearchService.countAntallSaksdokumenterMedian())

        antallSaksdokumenterMultiGauge.register(
            elasticsearchService.countAntallSaksdokumenterPerKlagebehandlingId().map {
                MultiGauge.Row.of(Tags.of("kbid", it.key.toString()), it.value)
            })
    }
}