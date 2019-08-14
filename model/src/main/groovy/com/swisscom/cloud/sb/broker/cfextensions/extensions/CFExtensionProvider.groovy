package com.swisscom.cloud.sb.broker.cfextensions.extensions

import com.swisscom.cloud.sb.broker.async.job.JobStatus


interface CFExtensionProvider {

    Collection<Extension> buildExtensions()

    String getApi()

    String getApi(List<String> tags, String url)

    JobStatus getJobStatus(Status status)
}