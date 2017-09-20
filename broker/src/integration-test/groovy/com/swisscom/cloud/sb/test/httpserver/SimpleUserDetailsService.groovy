package com.swisscom.cloud.sb.test.httpserver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
@Lazy
class SimpleUserDetailsService implements UserDetailsService {

    private final HttpServerConfig httpServerConfig

    @Autowired
    SimpleUserDetailsService(HttpServerConfig httpServerConfig) {
        this.httpServerConfig = httpServerConfig
    }

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == httpServerConfig.username) {
            return new User(httpServerConfig.username, httpServerConfig.password, Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
        }
        return null
    }

}