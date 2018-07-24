package com.github.quadflask.cnj.oauthserver.accounts

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Account(
        @Id
        @GeneratedValue
        var id: Long?,
        var username: String?,
        var password: String?,
        var active: Boolean?
) {
    constructor() : this(null, null, null, null)
    constructor(username: String, password: String, active: Boolean) : this(null, username, password, active)
}