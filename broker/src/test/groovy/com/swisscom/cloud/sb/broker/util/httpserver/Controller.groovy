package com.swisscom.cloud.sb.broker.util.httpserver

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class Controller {
    @RequestMapping("/")
    String index() {
        return "Test"
    }
}
