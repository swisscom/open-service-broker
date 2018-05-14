package com.swisscom.cloud.sb.broker.config

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class OnSystemPropertyCondition implements Condition {

    @Override
    boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnSystemProperty.class.getName())
        Boolean systemPropertyExistsCheck = (Boolean) attributes.get("exists")
        String systemProperty = (String) attributes.get("value")

        if ((systemPropertyExistsCheck && (System.getProperty(systemProperty) != null)) ||
                (!systemPropertyExistsCheck && (System.getProperty(systemProperty) == null))) {
            return true
        }
        return false
    }

}
