package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.api.KlagebehandlingListController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.Tag
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
class OpenApiConfig {

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.OAS_30)
            .select()
            .apis(RequestHandlerSelectors.basePackage(KlagebehandlingListController::class.java.packageName))
            .build()
            .pathMapping("/")
            .genericModelSubstitutes(ResponseEntity::class.java)
            .tags(Tag("klage-oppgave-api", "oppgave-api for saksbehandlere ved klageinstansen"))
    }

}