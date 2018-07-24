package com.github.quadflask.cnj.oauthserver.clients

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ClientRepository : JpaRepository<Client, Long> {
    fun findByClientId(clientId: String): Optional<Client>
}