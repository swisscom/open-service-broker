package com.swisscom.cloud.sb.broker.util

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.swisscom.cloud.sb.broker.error.ErrorCode
import groovy.transform.CompileStatic

import javax.validation.ConstraintViolation
import javax.validation.Validation

@CompileStatic
class DtoValidationHelper {
    static <T> T deserializeAndValidate(String json, Class<T> valueType) {
        try {
            def dto = new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, valueType)
            assertValidation(dto)
            return dto
        } catch (JsonParseException exception) {
            ErrorCode.SERVICEPROVIDER_INCORRECT_PARAMETERS.throwNew("JSON not valid")
        } catch (InvalidFormatException exception) {
            ErrorCode.SERVICEPROVIDER_INCORRECT_PARAMETERS.throwNew("Parameters parsing Error: ${exception.message}")
        } catch (UnrecognizedPropertyException exception) {
            ErrorCode.SERVICEPROVIDER_INCORRECT_PARAMETERS.throwNew("Parameters parsing Error: ${exception.message}")
        }
    }

    private static <T> void assertValidation(T dto) {
        def validationResult = Validation.buildDefaultValidatorFactory().getValidator().validate(dto)

        if (validationResult.any())
            ErrorCode.SERVICEPROVIDER_INCORRECT_PARAMETERS.throwNew(formatValidationResult(validationResult))
    }

    private static <T> String formatValidationResult(Set<ConstraintViolation<T>> validationResults) {
        List<String> validations = []
        validations.add("Request parameters validation errors: \n")
        for (ConstraintViolation<T> validationResult : validationResults)
            validations.add("- ${validationResult.propertyPath} : ${validationResult.message}".toString())

        validations.join('\n')
    }
}
