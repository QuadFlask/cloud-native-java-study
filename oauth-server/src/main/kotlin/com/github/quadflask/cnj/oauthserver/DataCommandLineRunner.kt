package com.github.quadflask.cnj.oauthserver

import com.github.quadflask.cnj.oauthserver.accounts.Account
import com.github.quadflask.cnj.oauthserver.clients.ClientRepository
import com.github.quadflask.cnj.oauthserver.accounts.AccountRepository
import com.github.quadflask.cnj.oauthserver.clients.Client
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.stream.Stream

@Component
class DataCommandLineRunner(
        val accountRepository: AccountRepository,
        val clientRepository: ClientRepository,
        val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String) {
//        mapOf("jlong" to "spring",
//                "dsyer" to "cloud",
//                "pwebb" to "boot",
//                "mminella" to "batch",
//                "rwinch" to "security")
//                .map { e -> e.apply { passwordEncoder.encode(this.value) } }
//                .forEach { (username, password) ->
//                    accountRepository.save(Account(username, password, true))
//                }


        Stream.of("dsyer,cloud", "pwebb,boot", "mminella,batch", "rwinch,security", "jlong,spring")
                .map { s -> s.split(",") }
                .forEach { tuple -> accountRepository.save(Account(tuple[0], passwordEncoder.encode(tuple[1]), true)) }

        Stream.of("html5,password", "android,secret").map { x -> x.split(",") }
                .forEach { x -> clientRepository.save(Client(x[0], passwordEncoder.encode(x[1]))) }
    }

}