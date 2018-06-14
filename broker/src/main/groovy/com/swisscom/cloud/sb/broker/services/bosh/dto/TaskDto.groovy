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

package com.swisscom.cloud.sb.broker.services.bosh.dto

import groovy.transform.CompileStatic

@CompileStatic
class TaskDto implements Serializable {
    int id
    State state
    String description
    int timestamp
    String result
    String user

    @Override
    public String toString() {
        return "TaskDto{" +
                "id=" + id +
                ", state=" + state +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", result='" + result + '\'' +
                ", user='" + user + '\'' +
                '}';
    }

    enum State {
        queued, processing, cancelled, cancelling, done, errored
    }
}
