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

package com.swisscom.cloud.sb.broker.util

import groovy.util.logging.Slf4j
import org.passay.CharacterData
import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import spock.lang.Specification

@Slf4j
class StringGeneratorSpec extends Specification {

    private final String upperCase = "A-Z"
    private final String lowerCase = "a-z"
    private final String numbers = "0-9"
    private final String defaultSpecial = "‘~!@#\$%^&*()_\\-+={}\\[\\]\\\\\\\\/<>,.;?':| "
    private final String customSpecial = "~!@"
    private final String defaultDisallowed = upperCase + lowerCase + numbers + defaultSpecial
    private final String customDisallowed = upperCase + lowerCase + numbers + customSpecial
    private int length = 30

    def "Check string generation with default rules and length"() {
        when:
        String numberOfCharacters = "(.{${length}})"
        String complexityPattern = atLeast(upperCase, 2) + atLeast(lowerCase, 2) + atLeast(numbers, 2) + atLeast(defaultSpecial, 2) + numberOfCharacters

        String password = StringGenerator.generateRandomStringWithRules()
        then:
        // check if password has default complexity
        password =~ /${complexityPattern}/
        // check if password has any disallowed characters
        password =~ /^[${defaultDisallowed}]*$/
    }

    def "Check string generation with default rules and length of 8"() {
        when:
        length = 8
        String numberOfCharacters = "(.{${length}})"
        String complexityPattern = atLeast(upperCase, 2) + atLeast(lowerCase, 2) + atLeast(numbers, 2) + atLeast(defaultSpecial, 2) + numberOfCharacters

        String password = StringGenerator.generateRandomStringWithRules(length)
        then:
        // check if password has default complexity
        password =~ /${complexityPattern}/
        // check if password has any disallowed characters
        password =~ /^[${defaultDisallowed}]*$/
    }

    def "Check string generation with custom rules and default length"() {
        when:
        final int occurrences = 4
        String numberOfCharacters = "(.{${length}})"
        String complexityPattern = atLeast(upperCase, occurrences) + atLeast(lowerCase, occurrences) + atLeast(numbers, occurrences) + atLeast(customSpecial, occurrences) + numberOfCharacters

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
                return customSpecial
            }
        }, occurrences)
        String password = StringGenerator.generateRandomStringWithRules(30, [atLeastFourLowerCase, atLeastFourUpperCase, atLeastFourNumbers, atLeastFourSpecialCharacters])

        then:
        // check if password has given complexity
        password =~ /${complexityPattern}/
        // check if password has any disallowed characters
        password =~ /^[${customDisallowed}]*$/
    }

    private String atLeast(String pattern, int repetitions) {
        String result = "(?=.*?"
        for (int i = 0; i < repetitions; i++) {
            result += "[" + pattern + "].*"
        }
        result + ")"
    }
}
