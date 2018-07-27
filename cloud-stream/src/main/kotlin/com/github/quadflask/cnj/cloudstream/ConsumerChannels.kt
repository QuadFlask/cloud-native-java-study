package com.github.quadflask.cnj.cloudstream

import org.springframework.cloud.stream.annotation.Input
import org.springframework.context.annotation.Profile
import org.springframework.messaging.SubscribableChannel

@Profile("consumer")
interface ConsumerChannels {
    companion object {
        const val DIRECTED = "directed"
        const val BROADCASTS = "broadcasts"
    }

    @Input(DIRECTED)
    fun directed(): SubscribableChannel

    @Input(BROADCASTS)
    fun broadcasts(): SubscribableChannel

}