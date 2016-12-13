package com.swisscom.cf.broker

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest
abstract class BaseSpecification extends Specification {
    protected String readTestFileContent(String testResource) {
        return new File(this.getClass().getResource(testResource).getFile()).text
    }
}
