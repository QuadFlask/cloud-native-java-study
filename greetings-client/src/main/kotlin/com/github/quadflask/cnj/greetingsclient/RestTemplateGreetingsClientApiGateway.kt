package com.github.quadflask.cnj.greetingsclient

import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/api")
class RestTemplateGreetingsClientApiGateway(@LoadBalanced val restTemplate: RestTemplate) {
    @GetMapping("/resttemplate/{name}")
    fun restTemplate(@PathVariable name: String): Map<String, String>? {
        return restTemplate.exchange<Map<String, String>>("http://greetings-service/greet/$name").body
    }
}

inline fun <reified T : Any> RestTemplate.exchange(url: String, method: HttpMethod = HttpMethod.GET, requestEntity: RequestEntity<*>? = null): ResponseEntity<T> {
    return this.exchange(url, method, requestEntity, object : ParameterizedTypeReference<T>() {})
}
