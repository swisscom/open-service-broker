package com.swisscom.cloud.sb.broker.cfapi.dto.jsonschema.v7

import com.fasterxml.jackson.databind.ObjectMapper
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification
import spock.lang.Unroll

class SchemaDtoSpec extends Specification {
    def readResourceContent(String resourcePath) {
        def clazz = getClass()
        def resource = clazz.getResource(resourcePath)
        def file = resource.getFile()
        return new File(file).text
    }

    void 'Should deserialize basic'() {
        given:
        String jsonString = readResourceContent("/jsonschema/schema-basic.json")
        def objectMapper = new ObjectMapper()

        when:
        SchemaDto result = objectMapper.readValue(jsonString, SchemaDto.class)

        then:
        noExceptionThrown()
        result != null
        result.title == "Person"
        result.properties.find { p -> p.key == "firstName"}.value.description == "The person's first name."
        result.properties.find { p -> p.key == "lastName"}.value.description == "The person's last name."
        result.properties.find { p -> p.key == "age"}.value.description == "Age in years which must be equal to or greater than zero."
    }

    @Unroll
    void 'Should reseriaize #fileName'() {
        given:
        String jsonString = readResourceContent("/jsonschema/schema-${fileName}.json")
        def objectMapper = new ObjectMapper()

        when:
        String result = objectMapper.writeValueAsString(objectMapper.readValue(jsonString, SchemaDto.class))

        then:
        noExceptionThrown()
        JSONAssert.assertEquals(jsonString, result, false)

        where:
        fileName << [ "basic", "arrayOfThings", "geographical", "001", "002", "003", "004", "005", "arcun"]
    }
}
