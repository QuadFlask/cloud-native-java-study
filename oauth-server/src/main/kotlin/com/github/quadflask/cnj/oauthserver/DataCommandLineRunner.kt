package com.github.quadflask.cnj.oauthserver

import com.github.quadflask.cnj.oauthserver.accounts.Account
import com.github.quadflask.cnj.oauthserver.clients.ClientRepository
import com.github.quadflask.cnj.oauthserver.accounts.AccountRepository
import com.github.quadflask.cnj.oauthserver.clients.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.stream.Stream

@Component
class DataCommandLineRunner(
        val accountRepository: AccountRepository,
        val clientRepository: ClientRepository
) : CommandLineRunner {

    override fun run(vararg args: String) {
        Stream.of("dsyer,cloud", "pwebb,boot", "mminella,batch", "rwinch,security", "jlong,spring")
                .map { s -> s.split(",") }
                .forEach { tuple -> accountRepository.save(Account(tuple[0], tuple[1], true)) }

        Stream.of("html5,password", "android,secret").map { x -> x.split(",") }
                .forEach { x -> clientRepository.save(Client(x[0], x[1])) }
    }

}