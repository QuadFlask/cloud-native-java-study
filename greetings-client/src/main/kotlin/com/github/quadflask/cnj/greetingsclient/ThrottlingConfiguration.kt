package com.github.quadflask.cnj.greetingsclient

import com.google.common.util.concurrent.RateLimiter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ThrottlingConfiguration {
    @Bean
    fun rateLimiter(): RateLimiter {
        return RateLimiter.create(1.0 / 10.0)
    }
}