package com.swisscom.cloud.sb.broker.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml

import javax.annotation.PostConstruct

//@Configuration
//@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker", ignoreInvalidFields = true)
@Component
class TemplateFactory {

    @Value('#{com.swisscom.cloud.sb.broker.templates}')
    List<Object> templates

    @PostConstruct
    void init() {
        templates.each {
            println("Template it = ${it}")
        }

        Yaml parser = new Yaml()
        def example = parser.loadAll(("/Users/zkhan/open-service-broker/broker/src/main/resources/application.yml" as File).text)

        println("example = ${example}")

        List exampleList = new ArrayList<>()
        example.each {
            println("it = ${it}")
            exampleList.add(it)
        }

        println("exampleList = ${exampleList}")
//        println("example = ${example.asCollection()}")
        println("exampleList[1].com.swisscom.cloud.sb.broker.templates = ${exampleList[1]}")
        println("exampleList[1][com.swisscom.cloud.sb.broker][templates] = ${exampleList[1]["com.swisscom.cloud.sb.broker"]["templates"]}")

        def templates = exampleList[1]["com.swisscom.cloud.sb.broker"]["templates"]
//        templates.each {
//            it.toString()
//        }

        def template1 = templates[0]

        FileWriter writer = new FileWriter("/Users/zkhan/open-service-broker/broker/src/main/groovy/com/swisscom/cloud/sb/broker/util/test.yaml")
        parser.dump(template1, writer)
        println(parser.dump(template1))

//    example.each{println it.subject}
    }
}
