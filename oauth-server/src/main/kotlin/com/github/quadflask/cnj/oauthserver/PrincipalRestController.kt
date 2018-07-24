package com.github.quadflask.cnj.oauthserver

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
class PrincipalRestController {
    @GetMapping("user")
    fun principal(principal: Principal): Principal = principal
}