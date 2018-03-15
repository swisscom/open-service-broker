package com.swisscom.cloud.sb.broker.error

import com.google.common.base.Strings
import groovy.transform.CompileStatic
import org.springframework.http.HttpStatus

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

@CompileStatic
enum ErrorCode {

    //TODO get rid of errorCodes
    SERVICE_INSTANCE_NOT_FOUND("69003", "Service Instance not found", "SC-SB-SI", HttpStatus.NOT_FOUND),
    SERVICE_INSTANCE_ALREADY_EXISTS("69004", "Service Instance already exists", "SC-SB-SI", HttpStatus.CONFLICT),
    SERVICE_BINDING_NOT_FOUND("69005", "Service Binding not found", "SC-SB-SERVICE-BINDING-NOT-FOUND", HttpStatus.NOT_FOUND),
    SERVICE_BINDING_ALREADY_EXISTS("69006", "Service Binding already exists", "SC-SB-SERVICE-BINDING-ALREADY-EXISTS", HttpStatus.CONFLICT),
    SERVICE_NOT_FOUND("69007", "Service not found", "SC-SB-SERVICE-NOT-FOUND", HttpStatus.BAD_REQUEST),
    PLAN_NOT_FOUND("69008", "Plan not found", "SC-SB-PLAN-NOT-FOUND", HttpStatus.BAD_REQUEST),
    RELATIONAL_DB_ALREADY_EXISTS("69009", "Database already exists", "SC-SB-DB-ALREADY-EXISTS", HttpStatus.INTERNAL_SERVER_ERROR),
    RELATIONAL_DB_NOT_FOUND("69010", "Database not found", "SC-SB-DB-NOT-FOUND", HttpStatus.INTERNAL_SERVER_ERROR),
    RELATIONAL_DB_USER_ALREADY_EXISTS("69011", "Database user already exists", "SC-SB-DB-USER-ALREADY-EXISTS", HttpStatus.INTERNAL_SERVER_ERROR),
    RELATIONAL_DB_USER_NOT_FOUND("69012", "Database user not found", "SC-SB-DB-USER-NOT-FOUND", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_MANAGER_AUTHENTICATION_FAILED("69014", "Service Manager authentication failed", "SC-SB-SM-AUTH-FAIL", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED),
    MONGODB_NOT_READY_YET("69015", "MongoDB is not ready yet. Please try again later", "SC-SB-MONGODB-NOT-READY", HttpStatus.SERVICE_UNAVAILABLE),
    PLAN_IN_USE("69016", "The plan you have tried to remove is in use", "SC-SB-PLAN-IN-USE", HttpStatus.CONFLICT),
    SERVICE_NOT_READY_YET("69017", "Service is not ready yet", "SC-SB-SERVICE-NOT-READY", HttpStatus.SERVICE_UNAVAILABLE),
    MONGODB_OPS_MANAGER_AUTHENTICATION_FAILED("69018", "MongoDB OpsManager authentication failed", "SC-SB-MONGODB-OPSM-AUTHENTICATION-FAIL", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED),
    MONGODB_ENTERPRISE_INSUFFICIENT_COMPUTING_RESOURCES("69019", "MongoDB Enterprise resource error. Please try again later or contact service provider", "SC-SB-MONGODB-ENTERPRISE-RESOURCE-ERROR", HttpStatus.SERVICE_UNAVAILABLE),
    MONGODB_OPS_MANAGER_UPDATE_FAILED("69020", "MongoDB OpsManager automation config update failed. Please try again later", "SC-SB-MONGODB-OPSM-UPDATE-FAIL", HttpStatus.SERVICE_UNAVAILABLE),
    ASYNC_REQUIRED("69021", "This service plan requires client support for asynchronous service operations.", "SB-ASYNC-REQUIRED", HttpStatus.UNPROCESSABLE_ENTITY),
    ASYNC_NOT_SUPPORTED("69022", "This service instance does *not* support asynchronous interactions", "SB-ASYNC-NOT-SUPPORTED", HttpStatus.UNPROCESSABLE_ENTITY),
    LAST_OPERATION_NOT_FOUND("69023", "Last operation not found", "SB-LAST-OP-NOT-FOUND", HttpStatus.GONE),
    SERVICE_IN_USE("69024", "The service you have tried to remove is in use", "SC-SB-PLAN-IN-USE", HttpStatus.CONFLICT),
    MAINTENANCE("69025", "Service broker is currently in maintenance, please try again later.", "SC-SB-MAINTENANCE", HttpStatus.SERVICE_UNAVAILABLE),
    SERVICE_INSTANCE_PROVISIONING_NOT_COMPLETED("69026", "Service instance provisioning not completed", "SC-SB-SI-NA", HttpStatus.PRECONDITION_FAILED),
    BACKUP_NOT_ENABLED("69027", "This service/plan does not allow backup/restore", "SC-SB-BKUP", HttpStatus.CONFLICT),
    BACKUP_LIMIT_EXCEEDED("69028", "You have exceeded the backup limit", "SC-SB-BKUP", HttpStatus.CONFLICT),
    BACKUP_NOT_FOUND("69029", "Backup not found", "SC-SB-BKUP", HttpStatus.NOT_FOUND),
    BACKUP_CONCURRENT_OPERATION("69030", "Another backup operation is in progress, please try again later", "SC-SB-BKUP", HttpStatus.CONFLICT),
    RESTORE_NOT_FOUND("69031", "Restore not found", "SC-SB-BKUP", HttpStatus.NOT_FOUND),
    RESTORE_NOT_ALLOWED("69032", "Restore not allowed ", "SC-SB-BKUP", HttpStatus.CONFLICT),
    API_SERVICE_INSTANCE_NOT_FOUND("69033", "No Service instance has been found", "SC-SB-API", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_INSTANCE_NOT_READY("69034", "Service instance not ready", "SC-SB-SI", HttpStatus.CONFLICT),
    RABBIT_MQ_VHOST_ALREADY_EXISTS("69035", "RabbitMq vhost already exists", "SC-SB-RABBITMQ", HttpStatus.CONFLICT),
    OAUTH2_CLIENT_CONFIGURATION_ERROR("69036", "Invalid UAA client configuration.", "SC-SB-OAUTH2", HttpStatus.BAD_REQUEST),
    SERVICE_INSTANCE_DELETED("69037", "Service Instance not found", "SC-SB-SI", HttpStatus.GONE),
    BILLING_INVALID_PARAMETER("69038", "One or more of the input parameters can not be processed.", "SC-SB-BI", HttpStatus.UNPROCESSABLE_ENTITY),
    BILLING_INFLUX_DB_EMPTY_RESPONSE("69039", "InfluxDB empty result", "SC-SB-BI", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UPDATE_NOT_ALLOWED("69040", "Service update is not allowed", "SC-SB-SI", UNPROCESSABLE_ENTITY),
    OPENWHISK_NAMESPACE_ALREADY_EXISTS("69041", "OpenWhisk namespace already exists", "SC-SB-OPENWHISK-NAMESPACE-ALREADY-EXISTS", HttpStatus.CONFLICT),
    OPENWHISK_CANNOT_CREATE_NAMESPACE("69042", "OpenWhisk cannot create subject", "SC-SB-OPENWHISK-CANNOT-CREATE-NAMESPACE", HttpStatus.BAD_REQUEST),
    OPENWHISK_SUBJECT_NOT_FOUND("69043", "OpenWhisk subject not found", "SC-SB-OPENWHISK-SUBJECT-NOT-FOUND", HttpStatus.BAD_REQUEST),
    CLOUDFOUNDRY_CONTEXT_REQUIRED("69044", "CloudFoundryContext required", "SC-SB-SI", HttpStatus.CONFLICT),
    PARENT_SERVICE_INSTANCE_NOT_FOUND("69045", "Parent service instance not found", "SC-SB-SI", HttpStatus.NOT_FOUND),
    UPDATE_INCORRECT_PLAN_ID("69046", "plan_id in previos_values is incorrect", "SC-SB-PLAN-INCORRECT-PLAN-ID", HttpStatus.BAD_REQUEST),
    OPERATION_IN_PROGRESS("69047", "Previous operation for this service instance is still in progress.", "SC-SB-SERVICE_OPERATION_IN_PROGRESS", UNPROCESSABLE_ENTITY),
    PLAN_UPDATE_NOT_ALLOWED("69048", "Updating of plan is not allowed", "SC-SB-PLAN-UPDATE-NOT-ALLOWED", HttpStatus.BAD_REQUEST),

    final String code
    final String errorCode
    final String description
    final HttpStatus httpStatus

    ErrorCode(String code, String description, String errorCode, HttpStatus httpStatus) {
        this.code = code
        this.description = description
        this.errorCode = errorCode
        this.httpStatus = httpStatus
    }

    void throwNew(String additionalDescription = "") {
        throw new ServiceBrokerException("${description}${Strings.isNullOrEmpty(additionalDescription) ? '' : ' ' + additionalDescription}", code, errorCode, httpStatus)
    }

    //Used for updating  https://gitlab.swisscloud.io/appc-cf-services/appc-cf-service-manager/blob/master/docs/error_codes.md
    static String toMdString() {
        StringBuilder sb = new StringBuilder()
        ErrorCode.values().each { ErrorCode it -> sb.append("\n| ").append(it.code).append(" | ").append(it.description).append(" | ").append(it.errorCode).append(" |") }
        sb.toString()
    }
}
