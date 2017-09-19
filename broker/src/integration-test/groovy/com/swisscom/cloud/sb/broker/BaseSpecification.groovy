package com.swisscom.cloud.sb.broker

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(properties = "spring.autoconfigure.exclude=com.swisscom.cloud.sb.broker.util.httpserver.WebSecurityConfig")
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "com.swisscom.cloud.sb.broker.util.httpserver.*"))
abstract class BaseSpecification extends Specification {
}
