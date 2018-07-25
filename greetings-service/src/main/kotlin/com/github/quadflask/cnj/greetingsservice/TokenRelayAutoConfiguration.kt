package com.github.quadflask.cnj.greetingsservice

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.security.oauth2.resource.DefaultUserInfoRestTemplateFactory
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateFactory
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.web.client.RestTemplate

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(EnableResourceServer::class)
class TokenRelayAutoConfiguration {
    @Configuration
    @Profile("nonsecure")
    class RestTemplateConfiguration {
        @Bean
        @LoadBalanced
        fun restTemplate(): RestTemplate = RestTemplate()
    }

    @Configuration
    class SecureRestTemplateConfiguration {
        @Bean
        @Lazy
        @LoadBalanced
        fun restTemplate(userInfoRestTemplateFactory: UserInfoRestTemplateFactory): OAuth2RestTemplate {
            return userInfoRestTemplateFactory.userInfoRestTemplate
        }
    }

    @Configuration
    @ConditionalOnClass(RequestInterceptor::class)
    @ConditionalOnBean(OAuth2ClientContextFilter::class)
    class FeignAutoConfiguration {
        @Bean
        fun requestInterceptor(clientContext: OAuth2ClientContext): RequestInterceptor {
            return RequestInterceptor { requestTemplate: RequestTemplate? ->
                requestTemplate?.header(HttpHeaders.AUTHORIZATION, "${clientContext.accessToken.tokenType} ${clientContext.accessToken.value}")
            }
        }
    }
}