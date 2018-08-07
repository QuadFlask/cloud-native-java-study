package com.github.quadflask.cnj.cloudstream

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.messaging.SubscribableChannel

@Profile("consumer")
@SpringBootApplication
@EnableBinding(ConsumerChannels::class)
class StreamConsumer {
    val log: Log = LogFactory.getLog(javaClass)

    fun incomingMessageFlow(incoming: SubscribableChannel, prefix: String): IntegrationFlow {
        return IntegrationFlows.from(incoming)
                .transform(String::class.java, String::toUpperCase)
                .handle(String::class.java) { greetings, _ ->
                    log.info("greeting received in IntegrationFlow ($prefix): $greetings")
                    null
                }.get()
    }

    @Bean
    fun direct(channels: ConsumerChannels): IntegrationFlow = incomingMessageFlow(channels.directed(), "directed")

    @Bean
    fun broadcast(channels: ConsumerChannels): IntegrationFlow = incomingMessageFlow(channels.broadcasts(), "broadcasts")

    @StreamListener(ConsumerChannels.DIRECTED)
    fun onNewDirectedGreetings(msg: String) {
        log.info("onNewDirectedGreetings: $msg")
    }

    @StreamListener(ConsumerChannels.BROADCASTS)
    fun onNewBroadcastGreetings(msg: String) {
        log.info("onNewBroadcastGreetings: $msg")
    }
}

fun main(args: Array<String>) {
    runApplication<StreamConsumer>(*args)
}

