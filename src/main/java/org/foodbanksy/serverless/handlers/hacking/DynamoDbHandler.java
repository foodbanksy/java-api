package org.foodbanksy.serverless.handlers.hacking;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.foodbanksy.serverless.ApiGatewayResponse;

import java.util.HashMap;
import java.util.Map;

@Log4j
public class DynamoDbHandler implements RequestHandler<DynamoDbHandler.PersonRequest, ApiGatewayResponse> {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PersonRequest {
        private String firstName;
        private String lastName;
    }

    @Data
    public static class PersonResponse {
        private String message;
    }

    private DynamoDB dynamoDb;

    public ApiGatewayResponse handleRequest(PersonRequest personRequest, Context context) {

        log.info("Context: " + context);

        this.initDynamoDbClient();

        personRequest = new PersonRequest("new", "guy");
        persistData(personRequest);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody("Hello " + personRequest)
                .setHeaders(headers)
                .build();
    }

    private PutItemOutcome persistData(PersonRequest personRequest)
            throws ConditionalCheckFailedException {
        String dynamodb_table = System.getenv("DYNAMODB_TABLE");
        if (dynamodb_table == null) {
            dynamodb_table = "java-api-dev";
        }

        return this.dynamoDb.getTable(dynamodb_table) // defined in serverless.yaml
                .putItem(
                        new PutItemSpec().withItem(new Item()
                                .withString("firstName", personRequest.getFirstName())
                                .withString("lastName", personRequest.getLastName())));
    }

    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
        this.dynamoDb = new DynamoDB(client);
    }
}
