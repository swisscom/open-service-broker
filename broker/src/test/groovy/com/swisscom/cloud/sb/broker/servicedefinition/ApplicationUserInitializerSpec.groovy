/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.servicedefinition

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.config.UserConfig
import com.swisscom.cloud.sb.broker.model.ApplicationUser
import com.swisscom.cloud.sb.broker.repository.ApplicationUserRepository
import com.swisscom.cloud.sb.broker.security.ApplicationUserInitializer
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Shared
import spock.lang.Specification

class ApplicationUserInitializerSpec extends Specification {
    private final String TEST_GUID = "TEST_GUID"

    private ApplicationUserRepository userRepository
    private ApplicationUserConfig applicationUserConfig

    @Shared
    PasswordEncoder passwordEncoder

    def setupSpec() {
        passwordEncoder = new NoOpPasswordEncoder()
    }

    def setup() {
        userRepository = Mock(ApplicationUserRepository)
    }

    def "Duplicated ApplicationUserConfiguration throws exception"() {
        applicationUserConfig = new ApplicationUserConfig()
        applicationUserConfig.platformUsers = new ArrayList<UserConfig>()

        applicationUserConfig.platformUsers.add(new UserConfig(username: "Username-Duplicated"))
        applicationUserConfig.platformUsers.add(new UserConfig(username: "Username-Unique"))
        applicationUserConfig.platformUsers.add(new UserConfig(username: "Username-Duplicated"))

        when:
        def sut = new ApplicationUserInitializer(userRepository, applicationUserConfig, passwordEncoder)
        sut.checkForDuplicatedApplicationUserConfigurations()

        then:
        def exception = thrown(Exception)
        exception.message.contains("Username-Duplicated")
    }

    def "Unique ApplicationUserConfiguration doesn't throw exception"() {
        applicationUserConfig = new ApplicationUserConfig()
        applicationUserConfig.platformUsers = new ArrayList<UserConfig>()

        applicationUserConfig.platformUsers.add(new UserConfig(username: "Username-Unique"))
        applicationUserConfig.platformUsers.add(new UserConfig(username: "Username-Unique2"))
        applicationUserConfig.platformUsers.add(new UserConfig(username: "Username-Unique3"))

        when:
        def sut = new ApplicationUserInitializer(userRepository, applicationUserConfig, passwordEncoder)
        sut.checkForDuplicatedApplicationUserConfigurations()

        then:
        noExceptionThrown()
    }

    def "User is deactivated if not present in ApplicationConfiguration"() {
        applicationUserConfig = new ApplicationUserConfig()
        applicationUserConfig.platformUsers = new ArrayList<UserConfig>()

        def usersList = new ArrayList<ApplicationUser>()
        def activeUser = new ApplicationUser(username: "activeUser", enabled: true)
        usersList.add(activeUser)

        userRepository.findAll() >> usersList

        when:
        def sut = new ApplicationUserInitializer(userRepository, applicationUserConfig, passwordEncoder)
        sut.synchronizeApplicationUsers()

        then:
        noExceptionThrown()
        activeUser.enabled == false
    }

    def "User is activated if present in ApplicationConfiguration"() {
        applicationUserConfig = new ApplicationUserConfig()
        applicationUserConfig.platformUsers = new ArrayList<UserConfig>()
        applicationUserConfig.platformUsers.add(new UserConfig(username: "deactivatedUser", password: "randomPassword"))

        def usersList = new ArrayList<ApplicationUser>()
        def deactivatedUser = new ApplicationUser(username: "deactivatedUser", enabled: false)
        usersList.add(deactivatedUser)

        userRepository.findAll() >> usersList

        when:
        def sut = new ApplicationUserInitializer(userRepository, applicationUserConfig, passwordEncoder)
        sut.synchronizeApplicationUsers()

        then:
        noExceptionThrown()
        deactivatedUser.enabled == true
    }

    def "UserInfo are updated from ApplicationConfiguration"() {
        applicationUserConfig = new ApplicationUserConfig()
        applicationUserConfig.platformUsers = new ArrayList<UserConfig>()
        applicationUserConfig.platformUsers.add(new UserConfig(username: "userUpdate", platformId: "new-id", password: "newPassword", role: "new-role"))

        def usersList = new ArrayList<ApplicationUser>()

        def oldPassword = passwordEncoder.encode("oldpassword")
        def oldUser = new ApplicationUser(username: "userUpdate", enabled: false, password: oldPassword, platformGuid: "old-id", role: "old-role")
        usersList.add(oldUser)

        userRepository.findAll() >> usersList

        when:
        def sut = new ApplicationUserInitializer(userRepository, applicationUserConfig, passwordEncoder)
        sut.synchronizeApplicationUsers()

        then:
        noExceptionThrown()
        oldUser.enabled == true
        oldUser.password == passwordEncoder.encode("newPassword")
        oldUser.platformGuid == "new-id"
        oldUser.role == "new-role"
    }

    def "User is added if not present in ApplicationConfiguration"() {
        applicationUserConfig = new ApplicationUserConfig()
        applicationUserConfig.platformUsers = new ArrayList<UserConfig>()
        def userConfig = new UserConfig(username: "newUser", platformId: "new-id", password: "newPassword", role: "new-role")
        applicationUserConfig.platformUsers.add(userConfig)

        def usersList = new ArrayList<ApplicationUser>()
        userRepository.findAll() >> usersList

        when:
        def sut = new ApplicationUserInitializer(userRepository, applicationUserConfig, passwordEncoder)
        sut.synchronizeApplicationUsers()

        then:
        noExceptionThrown()
        1 * userRepository.saveAndFlush {
            it.username == userConfig.username &&
                    it.platformGuid == userConfig.platformId &&
                    it.role == userConfig.role &&
                    it.password == passwordEncoder.encode(userConfig.password)
        }
    }
}
