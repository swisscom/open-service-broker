package com.swisscom.cloud.sb.broker.security

import com.swisscom.cloud.sb.broker.model.ApplicationUser
import com.swisscom.cloud.sb.broker.model.repository.ApplicationUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class ApplicationUserDetailsService implements UserDetailsService {

    final static ROLE_PREFIX = "ROLE_"

    @Autowired
    private ApplicationUserRepository userRepository

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ApplicationUser user = userRepository.findByUsername(username)
        if (!user) {
            throw new UsernameNotFoundException("User: ${username} is not registered")
        }
        List<GrantedAuthority> authorities = []
        authorities << new SimpleGrantedAuthority(ROLE_PREFIX + user.role)
        return new User(user.username, user.password, user.enabled, true, true, true, authorities)
    }
}
