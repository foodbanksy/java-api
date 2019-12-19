package org.foodbanksy.serverless.handlers.foodbank;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.foodbanksy.dynamodb.geo.FoodBankService;
import org.foodbanksy.serverless.ApiGatewayRequest;
import org.foodbanksy.serverless.ApiGatewayResponse;
import org.foodbanksy.serverless.model.FoodBank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FindFoodBankHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    private FoodBankService service = new FoodBankService();

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest request, Context context) {
        // This takes 3 query parameters:
        // latitude, longitude, rangeInMetres
        // Feel free to change rangeInMetres to miles if you fancy

        Map<String, String> params = request.getQueryStringParameters();
        double latitude = Double.parseDouble(params.get("latitude"));
        double longitude = Double.parseDouble(params.get("longitude"));
        double rangeInMetres = Double.parseDouble(params.get("rangeInMetres"));

        if(rangeInMetres > 250_000) {
            // Let's not go crazy - exception not caught so user won't see anything nice, but it'll stop dynomo getting nuked
            throw new IllegalArgumentException("Range too big. 250km maximum!");
        }

        log.info("Searching for Foodbanks, centred on {}:{}, range: {}", latitude, longitude, rangeInMetres);
        List<FoodBank> foodbanks = service.findFoodBanks(latitude, longitude, rangeInMetres);
        log.info("Found Foodbanks: {}", foodbanks);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(foodbanks)
                .setHeaders(headers)
                .build();
    }

    /**
     * Test method
     */
    public static void main(String[] args) {
        ApiGatewayRequest request = new ApiGatewayRequest();
        Map<String, String> params = new HashMap<>();
        params.put("latitude", "50");
        params.put("longitude", "0");
        params.put("rangeInMetres", "30000000");
        request.setQueryStringParameters(params);

        ApiGatewayResponse response = new FindFoodBankHandler().handleRequest(request, null);

        System.out.println(response);
    }

}
