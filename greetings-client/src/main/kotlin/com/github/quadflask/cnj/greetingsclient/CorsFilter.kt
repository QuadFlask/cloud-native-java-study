package com.github.quadflask.cnj.greetingsclient

import com.google.common.net.HttpHeaders
import com.google.common.util.concurrent.RateLimiter
import org.apache.commons.logging.LogFactory
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.discovery.event.HeartbeatEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.full.cast

@Component
class CorsFilter(val discoveryClient: DiscoveryClient) : Filter {
    private val log by lazy { LogFactory.getLog(javaClass) }
    private val catalog: MutableMap<String, List<ServiceInstance>> = ConcurrentHashMap()

    override fun init(filterConfig: FilterConfig?) {
        refreshCatalog()
    }

    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val request = HttpServletRequest::class.cast(req)
        val response = HttpServletResponse::class.cast(res)
        val originHeaderValue = originFor(request)
        val clientAllowed = isClientAllowed(originHeaderValue)

        if (clientAllowed) {
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, originHeaderValue)
        }
        chain.doFilter(req, res)
    }

    @EventListener(HeartbeatEvent::class)
    fun onHeartbeatEvent(event: HeartbeatEvent) {
        refreshCatalog()
    }

    private fun isClientAllowed(origin: String?): Boolean {
        if (StringUtils.hasText(origin)) {
            val originUri = URI.create(origin)
            val port = originUri.port
            val match = "${originUri.host}:${if (port <= 0) 80 else port}"

            catalog.forEach { k, v ->
                val collect = v.joinToString { si -> "${si.host}:${si.port}(${si.serviceId})" }
                log.info(collect)
            }

            val svcMatch = catalog.keys.any { serviceId ->
                catalog[serviceId]?.map { si -> "${si.host}:${si.port}" }?.any { hp -> hp.equals(match, true) } ?: false
            }

            return svcMatch
        }
        return false
    }

    private fun originFor(request: HttpServletRequest): String? {
        return if (StringUtils.hasText(request.getHeader(HttpHeaders.ORIGIN)))
            request.getHeader(HttpHeaders.ORIGIN)
        else request.getHeader(HttpHeaders.REFERER) ?: null
    }

    private fun refreshCatalog() {
        discoveryClient.services.forEach { svc ->
            catalog[svc] = discoveryClient.getInstances(svc)
        }
    }

    override fun destroy() {
    }
}