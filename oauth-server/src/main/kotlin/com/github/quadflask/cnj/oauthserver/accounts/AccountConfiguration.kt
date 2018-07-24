package com.github.quadflask.cnj.oauthserver.accounts

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

@Configuration
class AccountConfiguration {
    @Bean
    fun userDetailService(accountRepository: AccountRepository): UserDetailsService {
        return UserDetailsService { username ->
            accountRepository.findByUsername(username)
                    .map { account ->
                        val active = account.active ?: true
                        User(account.username, account.password,
                                active, active, active, active,
                                AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER"))
                    }
                    .orElseThrow { UsernameNotFoundException("username $username not found!") }
        }
    }
}