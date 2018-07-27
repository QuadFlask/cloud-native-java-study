package com.github.quadflask.cnj.cloudstream

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.messaging.support.MessageBuilder
import org.springframework.messaging.MessageChannel
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("simple")
@SpringBootApplication
@EnableBinding(ProducerChannels::class)
class SimpleStreamProducer

fun main(args: Array<String>) {
    runApplication<SimpleStreamProducer>(*args)
}

@Profile("simple")
@RestController
class GreetingProducer(channels: ProducerChannels) {
    val direct: MessageChannel by lazy { channels.directGreetings() }
    val broadcast: MessageChannel by lazy { channels.broadcastGreetings() }

    @RequestMapping("/hi/{name}")
    fun hi(@PathVariable name: String): ResponseEntity<String> {
        val message = "Hello, $name!"

        direct.send(MessageBuilder.withPayload("Direct: $message").build())
        broadcast.send(MessageBuilder.withPayload("Broadcast: $message").build())

        return ResponseEntity.ok(message)
    }
}

