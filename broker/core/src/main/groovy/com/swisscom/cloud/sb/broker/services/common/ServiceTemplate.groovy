package com.swisscom.cloud.sb.broker.services.common

public class ServiceTemplate {
    private String name
    private String version
    private List<String> templates

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getVersion() {
        return version
    }

    void setVersion(String version) {
        this.version = version
    }

    List<String> getTemplates() {
        return templates
    }

    void setTemplates(List<String> templates) {
        this.templates = templates
    }
}