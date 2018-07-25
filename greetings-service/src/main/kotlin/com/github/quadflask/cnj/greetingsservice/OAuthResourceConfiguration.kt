package com.github.quadflask.cnj.greetingsservice

import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer

@Configuration
@EnableResourceServer
@EnableOAuth2Client
class OAuthResourceConfiguration {
}