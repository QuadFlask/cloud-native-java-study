package com.github.quadflask.cnj.html5client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@SpringBootApplication
@EnableDiscoveryClient
@RestController
class Html5ClientApplication(val loadBalancerClient: LoadBalancerClient) {

    @GetMapping("/greetings-client-uri")
    fun greetingClientUri(): Map<String, String>? {
        return Optional
                .ofNullable(loadBalancerClient.choose("greetings-client"))
                .map { si -> Collections.singletonMap("uri", si.uri.toString()) }
                .orElse(null)
    }

}

fun main(args: Array<String>) {
    runApplication<Html5ClientApplication>(*args)
}
