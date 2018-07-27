package com.github.quadflask.cnj.cloudstream

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.IntegrationComponentScan
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("producer")
@SpringBootApplication
@EnableBinding(ProducerChannels::class)
@IntegrationComponentScan
class StreamProducer

fun main(args: Array<String>) {
    runApplication<StreamProducer>(*args)
}

@Profile("producer")
@MessagingGateway
interface GreetingGateway {
    @Gateway(requestChannel = ProducerChannels.BROADCAST)
    fun broadcastGreet(msg: String)

    @Gateway(requestChannel = ProducerChannels.DIRECT)
    fun directGreet(msg: String)
}

@Profile("producer")
@RestController
class GreetingProducer2(val gateway: GreetingGateway) {

    @RequestMapping("/hi/{name}")
    fun hi(@PathVariable name: String): ResponseEntity<String> {
        val message = "Hello, $name!"

        gateway.directGreet("Direct: $message")
        gateway.broadcastGreet("Broadcast: $message")

        return ResponseEntity.ok(message)
    }

}