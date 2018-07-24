package com.github.quadflask.cnj.oauthserver.clients

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.ClientRegistrationException
import org.springframework.security.oauth2.provider.client.BaseClientDetails
import java.util.*

@Configuration
class ClientConfiguration(val loadBalancerClient: LoadBalancerClient) {

    @Bean
    @Primary
    fun clientDetailService(clientRepository: ClientRepository): ClientDetailsService {
        return ClientDetailsService { clientId ->
            clientRepository.findByClientId(clientId).map { client ->
                val details = BaseClientDetails(client.clientId, null, client.scopes, client.authorizedGrantType, client.authorities)
                details.clientSecret = client.secret

                // details.setAutoApproveScopes(client.autoApproveScopes)

                val greetingsClientRedirectUri = Optional
                        .ofNullable(loadBalancerClient.choose("greetings-client"))
                        .map { si -> "http://${si.host}:${si.port}/" }
                        .orElseThrow { ClientRegistrationException("couldn't find and bind a greetings-client IP") }

                details.registeredRedirectUri = Collections.singleton(greetingsClientRedirectUri)

                details
            }.orElseThrow { ClientRegistrationException("no client ${clientId} registered!") }
        }
    }

}
