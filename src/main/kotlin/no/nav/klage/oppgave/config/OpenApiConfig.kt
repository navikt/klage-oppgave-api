package no.nav.klage.oppgave.config

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class OpenApiConfig {

    @Bean
    fun publicApi(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("kabal-api-public")
            .pathsToMatch("/api/**")
            .build()
    }

    @Bean
    fun adminApi(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("kabal-api-internal")
            .pathsToMatch("/**")
            .build()
    }

    @Bean
    fun springShopOpenAPI(): OpenAPI? {
        return OpenAPI()
            .info(
                Info().title("Kabal API")
                    .description("Kabal api")
                    .version("v0.0.1")
                    .license(License().name("Apache 2.0").url("http://springdoc.org"))
            )
            .externalDocs(
                ExternalDocumentation()
                    .description("Kabal API docs")
                    .url("https://springshop.wiki.github.org/docs")
            )
    }
}
