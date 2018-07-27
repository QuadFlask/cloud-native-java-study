package com.github.quadflask.cnj.messaging

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class MessagingApplication

fun main(args: Array<String>) {
    SpringApplicationBuilder(MessagingApplication::class.java)
            .web(WebApplicationType.NONE)
            .run(*args)
}
