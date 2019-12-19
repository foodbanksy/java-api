package org.foodbanksy.serverless.handlers.foodbank;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.foodbanksy.dynamodb.geo.GeoTable;
import org.foodbanksy.serverless.ApiGatewayRequest;
import org.foodbanksy.serverless.ApiGatewayResponse;
import org.foodbanksy.serverless.model.FoodBank;

import java.io.IOException;

@Slf4j
public class CreateFoodBankHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    private GeoTable table = new GeoTable();

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest request, Context context) {
        log.info("body: {}", request.getBody());
        FoodBank food;
        try {
            food = new ObjectMapper().readValue(request.getBody(), FoodBank.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Creating Foodbank: {}", food);
        table.createFoodBank(food);

        return ApiGatewayResponse.builder().setStatusCode(204).build();
    }

    /**
     * Test method
     */
    public static void main(String[] args) throws JsonProcessingException {
        ApiGatewayRequest request = new ApiGatewayRequest();
        request.setBody(new ObjectMapper().writeValueAsString(new FoodBank("Test Foodbank", 50.0d, 0.0d)));
        new CreateFoodBankHandler().handleRequest(request, null);
    }

}
