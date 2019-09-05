package com.swisscom.cloud.sb.broker.template;

import freemarker.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import static java.lang.String.format;

/**
 * An implementation of {@link TemplateEngine} which uses <a href="https://freemarker.apache.org/">Freemarker</a> as
 * template engine.
 *
 * @see <a href="https://freemarker.apache.org/docs/dgui_quickstart.html">Freemarker Getting started guide</a>
 */
public class FreeMarkerTemplateEngine implements TemplateEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreeMarkerTemplateEngine.class);

    private final Configuration freemarker;

    private FreeMarkerTemplateEngine(Configuration freemarker) {
        this.freemarker = freemarker;
    }

    public static FreeMarkerTemplateEngine of(Configuration configuration) {
        return new FreeMarkerTemplateEngine(configuration);
    }

    public static FreeMarkerTemplateEngine of(File directoryForTemplateLoading) {
        try {
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            configuration.setDirectoryForTemplateLoading(directoryForTemplateLoading);
            // Recommended settings for new projects:
            configuration.setDefaultEncoding("UTF-8");
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            configuration.setLogTemplateExceptions(false);
            configuration.setWrapUncheckedExceptions(true);
            return new FreeMarkerTemplateEngine(configuration);
        } catch (IOException e) {
            throw new IllegalStateException(format("Can't create %s", FreeMarkerTemplateEngine.class.getSimpleName()),
                                            e);
        }
    }

    @Override
    public String process(String templateId, Map<String, Object> modelMap) {
        LOGGER.trace("Processing '{}' with {}", templateId, modelMap);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Template deploymentTemplate = freemarker.getTemplate(templateId);
            Writer writer = new OutputStreamWriter(baos, Charset.forName("UTF-8"));
            deploymentTemplate.process(modelMap, writer);
            return baos.toString();

        } catch (TemplateNotFoundException e) {
            throw new IllegalArgumentException(e);

        } catch (IOException | TemplateException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FreeMarkerTemplateEngine.class.getSimpleName() + "[", "]")
                .add(Objects.toString(freemarker))
                .toString();
    }
}
