package com.swisscom.cloud.sb.broker.services.kubernetes.templates.comparator

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.stereotype.Component

import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
@Component
@CompileStatic
class NumberPrefixedStringComparator implements Comparator<String> {


    @Override
    int compare(String o1, String o2) {
        Pattern p = Pattern.compile("^([0-9]+).*")
        Matcher m = p.matcher(o1)
        Matcher m2 = p.matcher(o2)
        if (m.matches() && m2.matches()) {
            return Integer.valueOf(m.group(1)).compareTo(Integer.valueOf(m2.group(1)))
        }
        return -1
    }

}
