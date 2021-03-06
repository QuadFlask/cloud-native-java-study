package com.github.quadflask.cnj.greetingsservice

import org.springframework.cloud.client.serviceregistry.Registration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*

@RestController
class SecureGreetingsRestController(val registration: Registration) {

    @GetMapping("greet/{name}")
    fun hi(@PathVariable name: String, principal: Principal): Map<String, String> = Collections.singletonMap("greeting", "Hello, $name from ${principal.name}! (from: ${registration.serviceId}:${registration.port})")
}