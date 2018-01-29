package com.swisscom.cloud.sb.broker.security

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.model.ApplicationUser
import com.swisscom.cloud.sb.broker.model.repository.ApplicationUserRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@EnableConfigurationProperties
@Slf4j
class ApplicationUserInitializer {

    @Autowired
    private ApplicationUserRepository userRepository
    @Autowired
    private ApplicationUserConfig applicationUserConfig
    @Autowired
    private PasswordEncoder passwordEncoder

    @PostConstruct
    void init() throws Exception {
        checkForMissingUsers()
        addApplicationUsers()
    }

    void checkForMissingUsers() {
        List<ApplicationUser> users = userRepository.findAll()
        def dbUsernames = users.collect { it.username }

        def configUsernames = applicationUserConfig.applicationUsers.collect { it.username }
        if (configUsernames.size() != 0) {
            if (!configUsernames.containsAll(dbUsernames)) {
                throw new RuntimeException("Missing application user configuration exception. DB Username list - ${dbUsernames}")
            }
        }
    }

    void addApplicationUsers() {
        applicationUserConfig.applicationUsers.each {
            def u = userRepository.findByUsername(it.username)
            if (!u) {
                def user = new ApplicationUser()
                user.username = it.username
                user.password = passwordEncoder.encode(it.password)
                user.enabled = true
                user.role = it.role
                userRepository.saveAndFlush(user)
            }
        }
    }
}
