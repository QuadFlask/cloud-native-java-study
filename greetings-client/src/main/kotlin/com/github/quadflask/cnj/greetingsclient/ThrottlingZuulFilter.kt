package com.github.quadflask.cnj.greetingsclient

import com.google.common.util.concurrent.RateLimiter
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.exception.ZuulException
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils

@Component
class ThrottlingZuulFilter(private val rateLimiter: RateLimiter) : ZuulFilter() {

    private val tooManyRequests = HttpStatus.TOO_MANY_REQUESTS

    override fun filterType(): String = "pre"

    override fun filterOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    override fun shouldFilter(): Boolean = true

    override fun run(): Any? {
        try {
            val currentContext = RequestContext.getCurrentContext()
            val response = currentContext.response

            if (!rateLimiter.tryAcquire()) {
                response.contentType = MediaType.TEXT_PLAIN_VALUE
                response.status = tooManyRequests.value()

                currentContext.setSendZuulResponse(false)

                throw ZuulException(tooManyRequests.reasonPhrase, tooManyRequests.value(), tooManyRequests.reasonPhrase)
            }
        } catch (e: Exception) {
            ReflectionUtils.rethrowRuntimeException(e)
        }
        return null
    }
}