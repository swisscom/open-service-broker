package com.swisscom.cloud.sb.broker.backup.shield;

import org.immutables.value.Value;

import java.util.Collection;
import java.util.HashSet;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.join;

@Value.Immutable
public abstract class BackupDeregisterInformation {

    /**
     * @return names of deleted targets
     */
    @Value.Default
    public Collection<String> getDeletedTargets() {
        return new HashSet<>();
    }

    /**
     * @return names of deleted jobs
     */
    @Value.Default
    public Collection<String> getDeletedJobs() {
        return new HashSet<>();
    }

    @Value.Derived
    public int getNumberOfDeletedTargets() {
        return getDeletedTargets().size();
    }

    @Value.Derived
    public int getNumberOfDeletedJobs() {
        return getDeletedJobs().size();
    }

    @Value.Derived
    public boolean deletedSomething() {
        return getNumberOfDeletedJobs() > 0 || getNumberOfDeletedTargets() > 0;
    }

    public static class Builder extends ImmutableBackupDeregisterInformation.Builder {
    }

    public static BackupDeregisterInformation.Builder backupDeregisterInformation() {
        return new BackupDeregisterInformation.Builder();
    }

    @Override
    public String toString() {
        return format("Backup Deregistering deleted %d jobs: [%s] and %d targets: [%s]",
                      getNumberOfDeletedJobs(),
                      join(getDeletedJobs(), ","),
                      getNumberOfDeletedTargets(),
                      join(getDeletedTargets(), ","));
    }
}
