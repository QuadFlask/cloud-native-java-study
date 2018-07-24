package com.github.quadflask.cnj.greetingsclient

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class FeignGreetingsClientApiGateway(
        val greetingsClient: GreetingsClient
) {
    @GetMapping("/feign/{name}")
    fun feign(@PathVariable name: String): Map<String, String> {
        return greetingsClient.greet(name)
    }
}