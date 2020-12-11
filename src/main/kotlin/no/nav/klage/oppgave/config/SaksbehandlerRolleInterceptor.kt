package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.exceptions.NoSaksbehandlerRoleException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SaksbehandlerRolleInterceptor(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) : HandlerInterceptorAdapter() {


    @Throws(Exception::class)
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any?
    ): Boolean {
        val isSaksbehandler = innloggetSaksbehandlerRepository.erSaksbehandler()
        if (isSaksbehandler) {
            return true
        }
        throw NoSaksbehandlerRoleException("Bruker har ikke saksbehandlerrolle")
    }

}

@Configuration
class SaksbehandlerRolleInterceptorConfig(
    private val saksbehandlerRolleInterceptor: SaksbehandlerRolleInterceptor
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(saksbehandlerRolleInterceptor).addPathPatterns("/ansatte/**")
    }
}
