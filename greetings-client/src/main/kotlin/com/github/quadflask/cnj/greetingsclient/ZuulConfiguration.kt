package com.github.quadflask.cnj.greetingsclient

import org.apache.commons.logging.LogFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.cloud.netflix.zuul.filters.RouteLocator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableZuulProxy
class ZuulConfiguration {
    @Bean
    fun commandLineRunner(routeLocator: RouteLocator): CommandLineRunner {
        val log = LogFactory.getLog(javaClass)
        return CommandLineRunner {
            routeLocator.routes.forEach { r ->
                log.info("route: ${r.id} (${r.location}) ${r.fullPath}")
            }
        }
    }
}