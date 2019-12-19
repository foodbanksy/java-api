package org.foodbanksy.serverless.handlers.hacking;

import org.foodbanksy.serverless.ApiGatewayResponse;
import org.junit.jupiter.api.Test;

class DynamodbHandlerTest {
    @Test
    public void foo() {
        System.out.println(new DynamoDbHandler().handleRequest(new DynamoDbHandler.PersonRequest("first", "last"), null));
    }
}