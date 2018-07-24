package com.github.quadflask.cnj.greetingsservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class GreetingsServiceApplication

fun main(args: Array<String>) {
    runApplication<GreetingsServiceApplication>(*args)
}
