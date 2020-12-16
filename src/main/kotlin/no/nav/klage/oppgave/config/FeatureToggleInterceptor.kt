package no.nav.klage.oppgave.config

import no.finn.unleash.Unleash
import no.nav.klage.oppgave.config.FeatureToggleConfig.Companion.KLAGE_GENERELL_TILGANG
import no.nav.klage.oppgave.exceptions.FeatureNotEnabledException
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class FeatureToggleInterceptor(private val unleash: Unleash) : HandlerInterceptorAdapter() {

    @Throws(Exception::class)
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any?
    ): Boolean {
        val isEnabled = unleash.isEnabled(KLAGE_GENERELL_TILGANG)
        if (!isEnabled) {
            throw FeatureNotEnabledException("Du er ikke gitt tilgang til klage-oppgave-api")
        }
        return isEnabled
    }

}

@Configuration
class FeatureToggleInterceptorConfig(private val featureToggleInterceptor: FeatureToggleInterceptor) :
    WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(featureToggleInterceptor).addPathPatterns("/ansatte/**")
    }
}
