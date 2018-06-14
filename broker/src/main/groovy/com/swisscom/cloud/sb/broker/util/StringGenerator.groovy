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

import org.passay.CharacterData
import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.PasswordGenerator

import java.security.SecureRandom

class StringGenerator {
    public static final char[] alphaNumericCharset = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join().toCharArray()
    public static final char[] hexadecimalCharset = (('a'..'f') + ('0'..'9')).join().toCharArray()
    public static final char[] lowerAlphaCharset = ('a'..'z').join().toCharArray()

    static String randomAlphaNumericOfLength16() {
        random(16, alphaNumericCharset)
    }

    static String randomAlphaNumeric(int length) {
        random(length, alphaNumericCharset)
    }

    static String randomLowerAlphaOfLength16() {
        random(16, lowerAlphaCharset)
    }

    private static String random(int length, char[] chars) {
        org.apache.commons.lang.RandomStringUtils.random(length, 0, chars.length, false, false, chars, new SecureRandom())
    }

    static String randomUuid() {
        return UUID.randomUUID().toString()
    }

    static String randomHexadecimal(int length) {
        random(length, hexadecimalCharset)
    }

    static String generateRandomStringWithRules(int length = 30, List<CharacterRule> rules = null) {
        final int occurrences = 2
        if (!rules) {
            final CharacterRule atLeastTwoLowerCase = new CharacterRule(EnglishCharacterData.LowerCase, occurrences)
            final CharacterRule atLeastTwoUpperCase = new CharacterRule(EnglishCharacterData.UpperCase, occurrences)
            final CharacterRule atLeastTwoNumbers = new CharacterRule(EnglishCharacterData.Digit, occurrences)
            final CharacterRule atLeastTwoSpecialCharacters = new CharacterRule(new CharacterData() {
                @Override
                String getErrorCode() {
                    return "ERR_SPECIAL"
                }

                @Override
                String getCharacters() {
                    return "‘~!@#\$%^&*()_\\-+={}[\\]\\\\\\/<>,.;?':| "
                }
            }, occurrences)
            rules = [atLeastTwoLowerCase, atLeastTwoUpperCase, atLeastTwoNumbers, atLeastTwoSpecialCharacters]
        }
        PasswordGenerator generator = new PasswordGenerator()
        generator.generatePassword(length, rules)
    }
}
