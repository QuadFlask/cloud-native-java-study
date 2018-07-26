package com.github.quadflask.cnj.messaging

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.channel.MessageChannels
import org.springframework.messaging.MessageChannel

@Configuration
class BatchChannels {
    @Bean
    fun invalid(): MessageChannel = MessageChannels.direct().get()

    @Bean
    fun completed(): MessageChannel = MessageChannels.direct().get()
}