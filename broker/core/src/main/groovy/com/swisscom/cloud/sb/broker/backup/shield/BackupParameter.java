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

package com.swisscom.cloud.sb.broker.backup.shield;

import com.swisscom.cloud.sb.broker.config.BackupServiceConfig;
import org.immutables.value.Value;

import static org.immutables.value.Value.Style.ImplementationVisibility.PACKAGE;

@Value.Immutable
@Value.Style(visibility = PACKAGE, overshadowImplementation = true)
public abstract class BackupParameter implements BackupServiceConfig {
    public abstract String getStoreName();

    public abstract String getRetentionName();

    @Value.Default
    public String getScheduleName() {
        return "";
    }

    @Value.Default
    public String getSchedule() {
        return "";
    }

    public static class Builder extends ImmutableBackupParameter.Builder {}

    public static BackupParameter.Builder backupParameter() {
        return new BackupParameter.Builder();
    }
}
