package org.foodbanksy.dynamodb;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that starts a dynamodb local server and gracefully stops it after the tests have run
 */
public class DynamoDbExtension implements BeforeAllCallback, AfterAllCallback {
    private static DynamoDBProxyServer server;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        System.setProperty("sqlite4java.library.path", "native-libs");
        String port = "8000";
        server = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-inMemory", "-port", port});
        server.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        server.stop();
    }
}
