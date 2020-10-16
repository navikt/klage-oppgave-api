package no.nav.klage.oppgave.config

import no.finn.unleash.DefaultUnleash
import no.finn.unleash.FakeUnleash
import no.finn.unleash.Unleash
import no.finn.unleash.strategy.UserWithIdStrategy
import no.finn.unleash.util.UnleashConfig
import no.nav.klage.oppgave.service.unleash.ByProfileStrategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class FeatureToggleConfig {

    @Value("\${spring.application.name}")
    lateinit var appName: String

    @Value("\${spring.profiles.active}")
    lateinit var instance: String

    @Value("\${UNLEASH_URL}")
    private lateinit var unleashUrl: String

    @Bean
    @Profile("dev-gcp", "prod-gcp")
    fun unleash(byProfileStrategy: ByProfileStrategy): Unleash? {
        val unleashConfig = UnleashConfig.builder()
            .appName(appName)
            .instanceId(instance)
            .unleashAPI(unleashUrl)
            .build()
        return DefaultUnleash(unleashConfig, byProfileStrategy, UserWithIdStrategy())
    }

    @Bean
    @Profile("local")
    fun unleashMock(): Unleash? {
        val fakeUnleash = FakeUnleash()
        fakeUnleash.enableAll()
        return fakeUnleash
    }
}