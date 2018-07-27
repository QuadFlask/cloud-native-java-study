package com.github.quadflask.cnj.messaging

data class Contact(var fullName: String?, var email: String?, var validEmail: Boolean?, var id: Long?) {
    constructor() : this(null, null, null, null)
}