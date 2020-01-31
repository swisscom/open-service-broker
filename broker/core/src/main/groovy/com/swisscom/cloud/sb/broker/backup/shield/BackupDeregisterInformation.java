package com.swisscom.cloud.sb.broker.backup.shield;

import org.immutables.value.Value;

import static java.lang.String.format;

@Value.Immutable
public abstract class BackupDeregisterInformation {
    @Value.Default
    public int getDeletedTargets() {
        return 0;
    }

    @Value.Default
    public int getDeletedJobs() {
        return 0;
    }

    public static class Builder extends ImmutableBackupDeregisterInformation.Builder {}

    public static BackupDeregisterInformation.Builder backupDeregisterInformation() {
        return new BackupDeregisterInformation.Builder();
    }

    @Override
    public String toString() {
        return format("Backup Deregistering removed %d jobs and %d targets", getDeletedJobs(), getDeletedTargets());
    }
}
