package com.swisscom.cloud.sb.broker.services.genericserviceprovider

class GenericProvisionRequestPlanParameter {
    private String baseUrl
    private String username
    private String password
    private String serviceId
    private String planId

    public GenericProvisionRequestPlanParameter() {
        this.baseUrl = "";
        this.username = "";
        this.password = "";
        this.serviceId = "";
        this.planId = "";
    }

    public GenericProvisionRequestPlanParameter(String baseUrl, String username, String password, String serviceId, String planId) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.serviceId = serviceId;
        this.planId = planId;
    }

    public GenericProvisionRequestPlanParameter withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl
        return this
    }

    public GenericProvisionRequestPlanParameter withUsername(String username) {
        this.username = username
        return this
    }

    public GenericProvisionRequestPlanParameter withPassword(String password) {
        this.password = password
        return this
    }

    public GenericProvisionRequestPlanParameter withServiceId(String serviceId) {
        this.serviceId = serviceId
        return this
    }

    public GenericProvisionRequestPlanParameter withPlanId(String planId) {
        this.planId = planId
        return this
    }

    String getBaseUrl() {
        return baseUrl
    }

    String getUsername() {
        return username
    }

    String getPassword() {
        return password
    }

    String getServiceId() {
        return serviceId
    }

    String getPlanId() {
        return planId
    }
}
