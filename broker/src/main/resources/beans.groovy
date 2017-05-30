package com.swisscom.cloud.sb.broker

import com.swisscom.cloud.sb.broker.provisioning.ServiceInstanceCleanup


beans {
    flyway(org.flywaydb.core.Flyway) { bean ->
        table = 'FLYWAY_schema_version'
        dataSource = ref('dataSource')
        validateOnMigrate = true
        bean.initMethod = 'migrate'
    }

    cleanUpZombieServiceInstanceBean(ServiceInstanceCleanup) {}

    jobServiceInstanceCleanup(org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean) {
        targetObject = ref('cleanUpZombieServiceInstanceBean')
        targetMethod = 'cleanOrphanedServiceInstances'
    }

    triggerServiceInstanceCleanup(org.springframework.scheduling.quartz.CronTriggerFactoryBean) {
        jobDetail = ref('jobServiceInstanceCleanup')
        cronExpression = "0 0 0 * * ?" //once per day
    }


    quartzSchedulerWithPersistence(org.springframework.scheduling.quartz.SchedulerFactoryBean) { bean ->
        autoStartup = false
        dataSource = ref('dataSource')
        transactionManager = ref('transactionManager')
        configLocation = 'classpath:quartzWithDbPersistence.properties'
        schedulerName = 'quartzSchedulerWithPersistence'
        jobFactory = ref('autowiringSpringBeanJobFactory')
        startupDelay = 30
        waitForJobsToCompleteOnShutdown = true
        bean.dependsOn = 'flyway'
    }

    quartzScheduler(org.springframework.scheduling.quartz.SchedulerFactoryBean) { bean ->
        autoStartup = true
        configLocation = 'classpath:quartz.properties'
        schedulerName = 'quartzScheduler'
        jobFactory = ref('autowiringSpringBeanJobFactory')
        startupDelay = 30
        waitForJobsToCompleteOnShutdown = false
        bean.dependsOn = 'flyway'
        triggers = [ref('triggerServiceInstanceCleanup')]
    }
}