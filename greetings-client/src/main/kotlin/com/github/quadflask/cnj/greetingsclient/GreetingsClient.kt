package com.github.quadflask.cnj.greetingsclient

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(serviceId = "greetings-service")
interface GreetingsClient {

    @RequestMapping(method = [RequestMethod.GET], value = ["/greet/{name}"])
    fun greet(@PathVariable name: String): Map<String, String>

}