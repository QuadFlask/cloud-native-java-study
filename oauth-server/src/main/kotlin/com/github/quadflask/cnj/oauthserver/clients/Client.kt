package com.github.quadflask.cnj.oauthserver.clients

import org.springframework.util.StringUtils
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Client(
        @Id
        @GeneratedValue
        var id: Long?,
        var clientId: String?,
        var secret: String?,
        var scopes: String = StringUtils.arrayToCommaDelimitedString(arrayOf("openid")),
        var authorizedGrantType: String = StringUtils.arrayToCommaDelimitedString(arrayOf("authorization_code", "refresh_token", "password")),
        var authorities: String = StringUtils.arrayToCommaDelimitedString(arrayOf("ROLE_USER", "ROLE_ADMIN")),
        var autoApproveScopes: String = StringUtils.arrayToCommaDelimitedString(arrayOf(".*"))
) {
    constructor() : this(null, null, null)
    constructor(clientId: String, secret: String) : this(null, clientId, secret)
}