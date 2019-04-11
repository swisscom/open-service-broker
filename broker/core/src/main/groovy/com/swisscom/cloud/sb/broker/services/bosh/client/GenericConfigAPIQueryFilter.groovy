/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.services.bosh.client

import org.apache.http.client.utils.URIBuilder

/**
 * Generates URI query parameters for bosh director generic configs REST API
 * Bosh REST API Doc: https://bosh.io/docs/director-api-v1/#configs
 */
final class GenericConfigAPIQueryFilter {

    private final String name
    private final String type
    private final String latest

    private GenericConfigAPIQueryFilter(Builder builder) {
        name = builder.name
        type = builder.type
        latest = builder.latest
    }

    static class Builder {
        private String name = ''
        private String type = ''
        private String latest = ''

        Builder() {}

        Builder withName(String name) {
            this.name = name
            return this
        }

        Builder withType(String type) {
            this.type = type
            return this
        }

        Builder withLatest(boolean latest) {
            this.latest = latest.toString()
            return this
        }

        GenericConfigAPIQueryFilter build() {
            return new GenericConfigAPIQueryFilter(this)
        }
    }

    /**
     * Generates the URI query parameter string
     * @return Query parameters as URI string
     */
    String asUriString() {
        URIBuilder uriBuilder = new URIBuilder()
        if (!name.isEmpty()) uriBuilder.setParameter(FilterParameter.NAME.toString(), name)
        if (!type.isEmpty()) uriBuilder.setParameter(FilterParameter.TYPE.toString(), type)
        if (!latest.isEmpty()) uriBuilder.setParameter(FilterParameter.LATEST.toString(), latest)
        uriBuilder.toString()
    }

    enum FilterParameter {
        NAME("name"), TYPE("type"), LATEST("latest")

        private String value

        FilterParameter(final String value) {
            this.value = value
        }

        String getValue() {
            return value
        }

        @Override
        String toString() {
            return this.getValue()
        }
    }
}
