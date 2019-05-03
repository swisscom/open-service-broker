package com.swisscom.cloud.sb.broker.services.credhub


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import spock.lang.Specification

abstract class AbstractCredHubSpec extends Specification{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCredHubSpec.class)


    public static final DockerComposeContainer CREDHUB_UAA_MARIADB_CONTAINER

    static{
        CREDHUB_UAA_MARIADB_CONTAINER = new DockerComposeContainer(new File("docker/docker-compose-test.yml"))
                .withExposedService(
                        "mariadb",
                        3306,
                        Wait.forListeningPort())
                .withExposedService(
                        "credhub",
                        9000,
                        Wait.forListeningPort())
                .withExposedService(
                        "uaa",
                        9081,
                        Wait.forListeningPort()
                )
                .waitingFor("mariadb",
                            Wait.forLogMessage(".*mysqld: ready for connections.*", 2))
                .withLogConsumer("credhub", new Slf4jLogConsumer(LOG))
        CREDHUB_UAA_MARIADB_CONTAINER.start()
        LOG.info("'mariadb' running at {}:{}",
                 CREDHUB_UAA_MARIADB_CONTAINER.getServiceHost("mariadb", 3306),
                 CREDHUB_UAA_MARIADB_CONTAINER.getServicePort("mariadb", 3306))
        LOG.info("'credhub' running at {}:{}",
                 CREDHUB_UAA_MARIADB_CONTAINER.getServiceHost("credhub", 9000),
                 CREDHUB_UAA_MARIADB_CONTAINER.getServicePort("credhub", 9000))
        LOG.info("'uaa' running at {}:{}",
                 CREDHUB_UAA_MARIADB_CONTAINER.getServiceHost("uaa", 9081),
                 CREDHUB_UAA_MARIADB_CONTAINER.getServicePort("uaa", 9081))
    }

}
