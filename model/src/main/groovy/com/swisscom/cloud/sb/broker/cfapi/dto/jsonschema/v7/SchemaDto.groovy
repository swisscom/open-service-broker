package com.swisscom.cloud.sb.broker.cfapi.dto.jsonschema.v7

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
class SchemaDto {
    @JsonProperty("\$id")
    String schemaId

    @JsonProperty("\$schema")
    String schemaVersion

    @JsonProperty("\$ref")
    String reference

    @JsonProperty("\$comment")
    String comment

    Map<String, SchemaDto> definitions

    String type

    // any validations https://json-schema.org/latest/json-schema-validation.html#rfc.section.6.1.2
    @JsonProperty("enum")
    List<String> enumList

    // https://json-schema.org/latest/json-schema-validation.html#rfc.section.6.1.3
    @JsonProperty("const")
    Object constValue

    // https://json-schema.org/latest/json-schema-validation.html#rfc.section.6.2 -- Number
    Integer multipleOf

    Integer minimum

    Integer exclusiveMinimum

    Integer maximum

    Integer exclusiveMaximum

    // https://json-schema.org/latest/json-schema-validation.html#rfc.section.6.3 -- String
    Integer maxLength

    Integer minLength

    String pattern

    // https://json-schema.org/latest/json-schema-validation.html#rfc.section.7 -- format
    String format

    // https://json-schema.org/latest/json-schema-validation.html#rfc.section.6.4 -- Array
    SchemaDto items

    SchemaDto additionalItems

    Integer minItems

    Integer maxItems

    Boolean uniqueItems

    SchemaDto contains

    // https://json-schema.org/latest/json-schema-validation.html#rfc.section.6.5 -- Objects
    Integer maxProperties

    Integer minProperties

    Object patternProperties

    SchemaDto additionalProperties

    Object propertyName

    Map<String, SchemaDto> properties

    List<String> required

    // https://json-schema.org/latest/json-schema-validation.html#rfc.section.8 -- Encoding
    String contentEncoding

    String contentMediaType

    // https://json-schema.org/latest/json-schema-validation.html#rfc.section.10 -- Annotations
    String title

    String description

    @JsonProperty("default")
    Object defaultValue

    Boolean readOnly

    Boolean writeOnly

    List<Object> examples

    // https://json-schema.org/latest/json-schema-validation.html#rfc.section.6.6 -- Conditional Keywords
    @JsonProperty("if")
    SchemaDto ifCondition

    @JsonProperty("then")
    SchemaDto thenCondition

    @JsonProperty("else")
    SchemaDto elseCondition

    // https://json-schema.org/latest/json-schema-validation.html#rfc.section.6.7 -- restricting keywords
    List<SchemaDto> allOf

    List<SchemaDto> anyOf

    List<SchemaDto> oneOf

    SchemaDto not
}
