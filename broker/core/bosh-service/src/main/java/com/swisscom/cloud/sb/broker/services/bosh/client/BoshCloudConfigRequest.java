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

package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = ImmutableBoshCloudConfigRequest.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshCloudConfigRequest {

    @Value.Default
    public String getId() {
        return "";
    }

    @Value.Default
    public String getName() {
        return "";
    }

    @Value.Default
    public String getType() {
        return "";
    }

    @Value.Default
    public String getContent() {
        return "";
    }

    @Value.Lazy
    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize " + this);
        }
    }

    public static class Builder extends ImmutableBoshCloudConfigRequest.Builder {

    }

    public static Builder configRequest() {
        return new BoshCloudConfigRequest.Builder();
    }
}