package com.swisscom.cloud.sb.broker.util

import groovy.util.logging.Slf4j
import org.passay.CharacterData
import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import spock.lang.Specification

@Slf4j
class StringGeneratorSpec  extends Specification {

    def "Check string generation with default rules and length"() {
        when:
        String password = StringGenerator.generateRandomStringWithRules()
        log.info(password)

        then:
        // check if password has default complexity
        password =~ /(?=.*?[‘~!@#\u0024%^&*()_\-+={}\[\]\\\\/<>,.;?':| ].*[‘~!@#\u0024%^&*()_\-+={}\[\]\\\\/<>,.;?':| ])(?=.*?[a-z].*[a-z])(?=.*?[A-Z].*[A-Z])(?=.*?[0-9].*[0-9])(.{30})/
        // check if password has any disallowed characters
        password =~ /^[‘~!@#\u0024%^&*()_\-+={}\[\]\\\\\/<>,.;?':| a-zA-Z0-9]*$/
    }

    def "Check string generation with default rules and length of 8"() {
        when:
        String password = StringGenerator.generateRandomStringWithRules(8)
        log.info(password)

        then:
        // check if password has default complexity
        password =~ /(?=.*?[‘~!@#\u0024%^&*()_\-+={}\[\]\\\\/<>,.;?':| ].*[‘~!@#\u0024%^&*()_\-+={}\[\]\\\\/<>,.;?':| ])(?=.*?[a-z].*[a-z])(?=.*?[A-Z].*[A-Z])(?=.*?[0-9].*[0-9])(.{8})/
        // check if password has any disallowed characters
        password =~ /^[‘~!@#\u0024%^&*()_\-+={}\[\]\\\\\/<>,.;?':| a-zA-Z0-9]*$/
    }

    def "Check string generation with custom rules and default length"() {
        when:
        final int occurrences = 4
        final CharacterRule atLeastFourLowerCase = new CharacterRule(EnglishCharacterData.LowerCase, occurrences)
        final CharacterRule atLeastFourUpperCase = new CharacterRule(EnglishCharacterData.UpperCase, occurrences)
        final CharacterRule atLeastFourNumbers = new CharacterRule(EnglishCharacterData.Digit, occurrences)
        final CharacterRule atLeastFourSpecialCharacters = new CharacterRule(new CharacterData() {
            @Override
            String getErrorCode() {
                return "ERR_SPECIAL"
            }

            @Override
            String getCharacters() {
                return "~!@ "
            }
        }, occurrences)
        String password = StringGenerator.generateRandomStringWithRules(30, [atLeastFourLowerCase, atLeastFourUpperCase, atLeastFourNumbers, atLeastFourSpecialCharacters])
        log.info(password)
        final String upperCase = "(?=.*?[A-Z].*[A-Z].*[A-Z].*[A-Z])"
        final String lowerCase = "(?=.*?[a-z].*[a-z].*[a-z].*[a-z])"
        final String numbers = "(?=.*?[0-9].*[0-9].*[0-9].*[0-9])"
        final String specialCharacters = "(?=.*?[~!@ ].*[~!@ ].*[~!@ ].*[~!@ ])"

        then:
        // check if password has given complexity
        password =~ /${upperCase}${lowerCase}${numbers}${specialCharacters}/
    }
}
