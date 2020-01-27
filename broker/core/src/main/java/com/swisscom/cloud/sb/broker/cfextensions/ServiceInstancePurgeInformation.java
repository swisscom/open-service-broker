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

package com.swisscom.cloud.sb.broker.cfextensions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import javax.annotation.Nullable;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = ServiceInstancePurgeInformation.Builder.class)
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC, get = {"get*", "is*"})
@Value.Immutable
public abstract class ServiceInstancePurgeInformation {
    @JsonProperty("purged_service_instance_guid")
    public abstract String getPurgedServiceInstanceGuid();

    @JsonProperty("deleted_bindings")
    @Nullable
    public abstract Integer getDeletedBindings();

    @JsonProperty("is_system_backup_provider")
    @Nullable
    public abstract Boolean isSystemBackupProvider();

    @JsonProperty("errors")
    @Nullable
    public abstract Set<String> getErrors();

    public static class Builder extends ImmutableServiceInstancePurgeInformation.Builder {
    }

    public static ServiceInstancePurgeInformation.Builder serviceInstancePurgeInformation() {
        return new ServiceInstancePurgeInformation.Builder();
    }
}
