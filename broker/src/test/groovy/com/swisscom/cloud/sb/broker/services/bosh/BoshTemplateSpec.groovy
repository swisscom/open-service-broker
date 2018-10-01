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

package com.swisscom.cloud.sb.broker.services.bosh

import spock.lang.Specification

class BoshTemplateSpec extends Specification {

    public static final String MONGODB_TEMPLATE = '/bosh/mongodbent-bosh-template.yml'

    def "if there are not replaced placeholders validation should fail"() {
        given:
        BoshTemplate template = new BoshTemplate(readTemplate(MONGODB_TEMPLATE))

        when:
        template.build()
        then:
        thrown(Exception)
    }

    def "placeholders are replaced correctly"() {
        given:

        def input = """ director_uuid: BOSH_UID #STACK LEVEL
                        name: {{prefix}}-{{guid}} #SERVICE-INSTANCE LEVEL  e.g. <serviceid>
                        instance_groups:
                        - azs:
                          - z1
                          instances: 3
                          jobs:
                          - name: redis
                            properties:
                              redis-server:
                                databases: "{{databases}}_test" #SERVICE-INSTANCE LEVEL
                                instances: 3
                                master-name: {{guid}} #SERVICE-INSTANCE LEVEL
                                maxclients: "{{maxclients}}" #SERVICE-INSTANCE LEVEL
                                port: "{{redis-server-port}}" #SERVICE-INSTANCE LEVEL
                                security:
                                  require_pass: {{password}} #SERVICE-TEMPLATE LEVEL
                                sentinel-port: {{redis-sentinel-port}} #SERVICE-INSTANCE LEVEL
                                service-name: {{guid}} #SERVICE-INSTANCE LEVEL
                                timeout: 10
                                config-command: {{config-command}} #SERVICE-INSTANCE LEVEL
                                slaveof-command: {{slaveof-command}} #SERVICE-INSTANCE LEVEL"""
        def expected = """ director_uuid: BOSH_UID #STACK LEVEL
                        name: prefix-guid #SERVICE-INSTANCE LEVEL  e.g. <serviceid>
                        instance_groups:
                        - azs:
                          - z1
                          instances: 3
                          jobs:
                          - name: redis
                            properties:
                              redis-server:
                                databases: "databases_test" #SERVICE-INSTANCE LEVEL
                                instances: 3
                                master-name: guid #SERVICE-INSTANCE LEVEL
                                maxclients: maxclients #SERVICE-INSTANCE LEVEL
                                port: redis-server-port #SERVICE-INSTANCE LEVEL
                                security:
                                  require_pass: password #SERVICE-TEMPLATE LEVEL
                                sentinel-port: redis-sentinel-port #SERVICE-INSTANCE LEVEL
                                service-name: guid #SERVICE-INSTANCE LEVEL
                                timeout: 10
                                config-command: config-command #SERVICE-INSTANCE LEVEL
                                slaveof-command: slaveof-command #SERVICE-INSTANCE LEVEL"""
        BoshTemplate template = new BoshTemplate(input)
        and:
        template.replace('prefix', 'prefix')
        template.replace('guid', 'guid')
        template.replace('databases', 'databases')
        template.replace('maxclients', 'maxclients')
        template.replace('redis-server-port', 'redis-server-port')
        template.replace('password', 'password')
        template.replace('redis-sentinel-port', 'redis-sentinel-port')
        template.replace('config-command', 'config-command')
        template.replace('slaveof-command', 'slaveof-command')
        when:
        def output = template.build()

        then:
        output == expected
    }

    def "instance number is counted correctly"() {
        expect:
        new BoshTemplate(readTemplate(MONGODB_TEMPLATE)).instanceCount() == 3
    }

    def "azs are shuffled correctly"() {
        given:
        def template = new BoshTemplate(readTemplate('/bosh/az-test-bosh-template.yml'))
        and:
        ['bosh-director-uuid', 'mms-api-key', 'mms-base-url', 'mms-group-id', 'guid', 'port', 'mongodb-binary-path',
         'health-check-user', 'health-check-password', 'plan', 'prefix', ''].each { template.replace(it, it) }
        when:
        template.shuffleAzs()
        def result = template.build()
        then:
        noExceptionThrown()
        result
    }

    private String readTemplate(String file) {
        return new File(this.getClass().getResource(file).getFile()).text
    }
}
