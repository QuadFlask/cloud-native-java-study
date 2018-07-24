package com.github.quadflask.cnj.greetingsclient

import org.apache.commons.logging.LogFactory
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.discovery.event.HeartbeatEvent
import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent
import org.springframework.cloud.netflix.zuul.filters.RouteLocator
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class RoutesListener(
        val routeLocator: RouteLocator,
        val discoveryClient: DiscoveryClient
) {
    private val log by lazy { LogFactory.getLog(javaClass) }

    @EventListener(HeartbeatEvent::class)
    fun onHeartbeatEvent(event: HeartbeatEvent) {
        log.info("onHeartbeatEvent")
        discoveryClient.services.joinToString(" ").let(log::info)
    }

    @EventListener(RoutesRefreshedEvent::class)
    fun onRoutesRefreshedEvent(event: RoutesRefreshedEvent) {
        log.info("onRoutesRefreshedEvent")
        routeLocator.routes.joinToString(" ").let(log::info)
    }
}