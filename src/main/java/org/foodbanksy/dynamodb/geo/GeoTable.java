package org.foodbanksy.dynamodb.geo;

import com.amazonaws.geo.GeoDataManager;
import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.geo.model.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.model.*;
import lombok.extern.slf4j.Slf4j;
import org.foodbanksy.serverless.model.FoodBank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This is a bit of a mess and could do with splitting into generic "lets-store-some-points" logic and some specific
 * "lets-store-a-foodbank" logic.
 */
@Slf4j
public class GeoTable {

    private DynamoDB dynamoDb;
    private GeoDataManager geoDataManager;

    public GeoTable() {
        init();
    }

    private String getTableName() {
        String fromEnv = System.getenv("DYNAMODB_TABLE");
        return fromEnv != null ? fromEnv : "java-api-dev";
    }

    private void init() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
        this.dynamoDb = new DynamoDB(client);

        GeoDataManagerConfiguration config = new GeoDataManagerConfiguration((AmazonDynamoDBClient) client, "geo-test");
        this.geoDataManager = new GeoDataManager(config);

        final String tableName = getTableName();
        config = new GeoDataManagerConfiguration((AmazonDynamoDBClient) client, tableName);
        geoDataManager = new GeoDataManager(config);

        try {
            // Try to create the table. This should only need to be done once ever
            CreateTableRequest createTableRequest = GeoTableUtil.getCreateTableRequest(config);
            config.getDynamoDBClient().createTable(createTableRequest);
            log.info("Creating DynamoDB table {}", getTableName());
            waitForTableToBeReady();
        } catch (ResourceInUseException e) {
            // Table already exists
            System.out.println(e);
        }
    }

    private void waitForTableToBeReady() {
        log.info("Waiting for table to be ready");
        GeoDataManagerConfiguration config = geoDataManager.getGeoDataManagerConfiguration();

        DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(config.getTableName());
        DescribeTableResult describeTableResult = config.getDynamoDBClient().describeTable(describeTableRequest);

        while (!describeTableResult.getTable().getTableStatus().equalsIgnoreCase("ACTIVE")) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            describeTableResult = config.getDynamoDBClient().describeTable(describeTableRequest);
        }
        log.info("Table is ready");
    }

    public void createFoodBank(FoodBank foodBank) {
        GeoPoint geoPoint = new GeoPoint(foodBank.getLatitude(), foodBank.getLongitude());
        // I don't know what this next line is for. Uniqueness?
        AttributeValue rangeKeyAttributeValue = new AttributeValue().withS(UUID.randomUUID().toString());
        AttributeValue foodbankNameAttributeValue = new AttributeValue().withS(foodBank.getName());

        PutPointRequest putPointRequest = new PutPointRequest(geoPoint, rangeKeyAttributeValue);
        putPointRequest.getPutItemRequest().addItemEntry("name", foodbankNameAttributeValue);

        PutPointResult putPointResult = geoDataManager.putPoint(putPointRequest);

        log.debug("Point: {}", putPointResult);
    }

    public PutItemOutcome createOnePoint() throws ConditionalCheckFailedException {
//        putPoint(50.0, 0.0);
        return null;
    }

    public void queryRadius() {
        GeoPoint centerPoint = new GeoPoint(50.0d, 0.0d);
        double radiusInMeter = 100000;

        List<String> attributesToGet = new ArrayList<String>();
        attributesToGet.add("rangeKey");
        attributesToGet.add("geoJson");
        attributesToGet.add("foodbankname");

        QueryRadiusRequest queryRadiusRequest = new QueryRadiusRequest(centerPoint, radiusInMeter);
        queryRadiusRequest.getQueryRequest().setAttributesToGet(attributesToGet);
        QueryRadiusResult queryRadiusResult = geoDataManager.queryRadius(queryRadiusRequest);

        log.info("Result: {}", queryRadiusResult);
    }


    private void putPoint(double latitude, double longitute) {
        GeoPoint geoPoint = new GeoPoint(latitude, longitute);
        AttributeValue rangeKeyAttributeValue = new AttributeValue().withS(UUID.randomUUID().toString());
        AttributeValue schoolNameKeyAttributeValue = new AttributeValue().withS("mybank");

        PutPointRequest putPointRequest = new PutPointRequest(geoPoint, rangeKeyAttributeValue);
        putPointRequest.getPutItemRequest().addItemEntry("foodbankname", schoolNameKeyAttributeValue);

        PutPointResult putPointResult = geoDataManager.putPoint(putPointRequest);

        log.info("Point: {}", putPointRequest);
    }

    private void getPoint(double latitude, double longitute) {
//        GeoPoint geoPoint = new GeoPoint(latitude, longitute);
//        AttributeValue rangeKeyAttributeValue = new AttributeValue().withS(requestObject.getString("rangeKey"));
//
//        GetPointRequest getPointRequest = new GetPointRequest(geoPoint, rangeKeyAttributeValue);
//        GetPointResult getPointResult = geoDataManager.getPoint(getPointRequest);
    }
}
