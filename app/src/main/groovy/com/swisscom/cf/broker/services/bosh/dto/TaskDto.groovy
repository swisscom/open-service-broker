package com.swisscom.cf.broker.services.bosh.dto

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
