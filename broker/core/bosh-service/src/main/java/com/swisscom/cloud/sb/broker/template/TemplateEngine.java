package com.swisscom.cloud.sb.broker.template;

import java.util.Map;

/**
 * A {@link TemplateEngine} processes files that use some template notation, substituting the placeholders by the passed
 * variables and returning text.
 */
public interface TemplateEngine {

    /**
     * Substitute in certain template identified by its name, the placeholders passed as a {@link Map}
     *
     * @param templateId the name of the template to use
     * @param modelMap   a {@link Map} which keys are the placeholders in the template and which values are the values
     *                   to set in those placeholders
     * @return the substituted text
     */
    String process(String templateId, Map<String, Object> modelMap);
}
