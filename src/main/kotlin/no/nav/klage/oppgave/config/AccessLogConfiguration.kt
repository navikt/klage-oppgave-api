package no.nav.klage.oppgave.config

import ch.qos.logback.access.tomcat.LogbackValve
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Files

@Configuration
class AccessLogConfiguration {

    @Bean
    fun addLogbackAccessValve() = TomcatContextCustomizer { context ->

        javaClass.getResourceAsStream("/logback-access.xml").use {
            Files.createDirectories(
                (context.catalinaBase.toPath()
                    .resolve(LogbackValve.DEFAULT_CONFIG_FILE)).parent
            )

            Files.copy(
                it, context.catalinaBase.toPath()
                    .resolve(LogbackValve.DEFAULT_CONFIG_FILE)
            )
        }

        LogbackValve().let {
            it.isQuiet = true
            context.pipeline.addValve(it)
        }
    }

}