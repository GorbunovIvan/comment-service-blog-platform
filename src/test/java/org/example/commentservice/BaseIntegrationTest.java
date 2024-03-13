package org.example.commentservice;

import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class BaseIntegrationTest {

    @Container
    private static final MongoDBContainer mongoDB = new MongoDBContainer(DockerImageName.parse("mongo"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry propertyRegistry) {

        var connectionString = mongoDB.getConnectionString();
        var port = retrievePortFromConnectionString(connectionString);

        propertyRegistry.add("spring.data.mongodb.host", mongoDB::getHost);
        propertyRegistry.add("spring.data.mongodb.port", () -> port);
        propertyRegistry.add("spring.data.mongodb.database", () -> "testdb-comment-service-blog-platform");
    }

    private static int retrievePortFromConnectionString(@Nonnull String connectionString) {

        var indexOfStartOfPort = connectionString.lastIndexOf(":");
        if (indexOfStartOfPort == -1) {
            throw new IllegalStateException("No port in found in mongoDB connection string");
        }
        indexOfStartOfPort += 1;

        var portString = connectionString.substring(indexOfStartOfPort);
        return Integer.parseInt(portString);
    }

    @BeforeAll
    static void beforeAll() {
        mongoDB.start();
    }

    @AfterAll
    static void afterAll() {
        mongoDB.stop();
    }
}
