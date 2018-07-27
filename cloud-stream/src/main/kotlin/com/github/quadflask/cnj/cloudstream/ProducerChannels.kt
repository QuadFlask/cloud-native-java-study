package com.github.quadflask.cnj.cloudstream

import org.springframework.cloud.stream.annotation.Output
import org.springframework.context.annotation.Profile
import org.springframework.messaging.MessageChannel

@Profile("producer")
interface ProducerChannels {

    companion object {
        const val DIRECT = "directGreetings"
        const val BROADCAST = "broadcastGreetings"
    }

    @Output(DIRECT) // 채널 이름
    fun directGreetings(): MessageChannel

    @Output(BROADCAST) // 채널 이름
    fun broadcastGreetings(): MessageChannel

}