package com.swisscom.cloud.sb.broker.security

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.config.UserConfig
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
        synchronizeApplicationUsers()
    }

    void disableApplicationUser(ApplicationUser user) {
        if (!user.enabled)
        {
            user.enabled = false;
            userRepository.saveAndFlush(user);
        }
    }

    void synchronizeApplicationUsers() {
        def allDbUser = userRepository.findAll();

        applicationUserConfig.platformUsers.each {
            platformGroup ->
                platformGroup.users.each {
                    configUser ->
                        def dbUser = allDbUser.findOne { user -> user.username == configUser.username && user.platformGuid == platformGroup.guid }
                        if (dbUser == null)
                        {
                            addApplicationUser(platformGroup.guid, configUser)
                        }
                        else
                        {
                            synchronizeApplicationUser(dbUser, configUser)
                            allDbUser.remove(dbUser);
                        }
            }
        }

        allDbUser.each { oldUser -> disableApplicationUser(oldUser) };
    }

    void synchronizeApplicationUser(ApplicationUser user, UserConfig userConfig) {
        def changed = false

        if (user.password != passwordEncoder.encode(userConfig.password)) {
            user.password = passwordEncoder.encode(userConfig.password)
            changed = true;
        }

        if (user.role != userConfig.role) {
            user.role = userConfig.role
            changed = true;
        }

        if (!user.enabled)
        {
            user.enabled = true;
            changed = true;
        }

        if (changed)
        {
            userRepository.saveAndFlush(user);
        }
    }

    void addApplicationUser(String platformGuid, UserConfig userConfig) {
        user = new ApplicationUser()
        user.username = userConfig.username
        user.password = passwordEncoder.encode(userConfig.password)
        user.enabled = true
        user.role = userConfig.role
        user.platformGuid = platformGuid
        userRepository.saveAndFlush(user)
    }
}
