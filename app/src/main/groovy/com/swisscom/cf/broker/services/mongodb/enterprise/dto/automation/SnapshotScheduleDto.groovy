package com.swisscom.cf.broker.services.mongodb.enterprise.dto.automation

class SnapshotScheduleDto implements Serializable {
    int snapshotIntervalHours
    int snapshotRetentionDays
    int dailySnapshotRetentionDays
    int weeklySnapshotRetentionWeeks
    int monthlySnapshotRetentionMonths
    int pointInTimeWindowHours

    static constraints = {
        snapshotIntervalHours inList: [6, 8, 12, 24]
        snapshotRetentionDays range: 1..5
        dailySnapshotRetentionDays range: 1..365
        weeklySnapshotRetentionWeeks range: 1..52
        monthlySnapshotRetentionMonths range: 1..36
    }
}