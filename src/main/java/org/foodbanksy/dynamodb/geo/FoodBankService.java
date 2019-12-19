package org.foodbanksy.dynamodb.geo;

import com.amazonaws.geo.model.GeoPoint;
import com.amazonaws.geo.util.GeoJsonMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.foodbanksy.serverless.model.FoodBank;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FoodBankService {

    private final GeoTable table = new GeoTable();

    public void createFoodBank(FoodBank foodBank) {
        table.createPoint(foodBank.getName(), foodBank.getLatitude(), foodBank.getLongitude());
    }

    /**
     * Finds a list of {@link FoodBank}s in a given circular area
     * @param latitude latitude of the centre of the search area
     * @param longitude longitude of the centre of the search area
     * @param rangeInMetres radius in metres of the search area
     */
    public List<FoodBank> findFoodBanks(double latitude, double longitude, double rangeInMetres) {
        List<Map<String, AttributeValue>> queryResults = table.queryRadius(latitude, longitude, rangeInMetres);
        return queryResults.stream()
                .map(FoodBankService::toFoodBank)
                .collect(Collectors.toList());
    }

    private static FoodBank toFoodBank(Map<String, AttributeValue> result) {
        GeoPoint point = GeoJsonMapper.geoPointFromString(result.get("geoJson").getS());
        return new FoodBank(result.get("name").getS(), point.getLatitude(), point.getLongitude());
    }
}
