package com.microsoft.semantickernel.tests.connectors.memory.jdbc;

import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStore;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.connectors.data.jdbc.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.tests.connectors.memory.Hotel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class JDBCVectorStoreTest {
    @Container
    private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>("mysql:5.7.34");
    private static final String MYSQL_USER = "test";
    private static final String MYSQL_PASSWORD = "test";
    private static Connection connection;

    @BeforeAll
    static void setup() throws SQLException {
        connection = DriverManager.getConnection(CONTAINER.getJdbcUrl(), MYSQL_USER, MYSQL_PASSWORD);
    }

    @Test
    public void getCollectionNamesAsync() {
        JDBCVectorStoreOptions options = JDBCVectorStoreOptions.builder()
                .withQueryProvider(MySQLVectorStoreQueryProvider.builder()
                        .withConnection(connection)
                        .build())
                .build();

        JDBCVectorStore vectorStore = JDBCVectorStore.builder()
                .withConnection(connection)
                .withOptions(options)
                .build();

        vectorStore.getCollectionNamesAsync().block();

        List<String> collectionNames = Arrays.asList("collection1", "collection2", "collection3");

        for (String collectionName : collectionNames) {
            vectorStore.getCollection(collectionName, Hotel.class, null).createCollectionAsync().block();
        }

        List<String> retrievedCollectionNames = vectorStore.getCollectionNamesAsync().block();
        assertNotNull(retrievedCollectionNames);
        assertEquals(collectionNames.size(), retrievedCollectionNames.size());
        for (String collectionName : collectionNames) {
            assertTrue(retrievedCollectionNames.contains(collectionName));
        }
    }
}
