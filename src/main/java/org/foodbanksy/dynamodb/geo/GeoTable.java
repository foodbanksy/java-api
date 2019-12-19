package org.foodbanksy.dynamodb.geo;

import com.amazonaws.geo.GeoDataManager;
import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.geo.model.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This is a bit of a mess and could do with splitting into generic "lets-store-some-points" logic and some specific
 * "lets-store-a-foodbank" logic.
 */
@Slf4j
public class GeoTable {
    public static final String COLUMN_RANGE_KEY = "rangeKey";
    public static final String COLUMN_GEO_JSON = "geoJson";
    public static final String COLUMN_NAME = "name";
    private GeoDataManager geoDataManager;

    public GeoTable() {
        init();
    }

    private String getTableName() {
        String fromEnv = System.getenv("DYNAMODB_TABLE");
        return fromEnv != null ? fromEnv : "java-api-dev";
    }

    private void init() {
        final String tableName = getTableName();
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
        GeoDataManagerConfiguration config = new GeoDataManagerConfiguration((AmazonDynamoDBClient) client, tableName);
        geoDataManager = new GeoDataManager(config);

        try {
            // Try to create the table. This should only need to be done once ever
            CreateTableRequest createTableRequest = GeoTableUtil.getCreateTableRequest(config);
            config.getDynamoDBClient().createTable(createTableRequest);
            log.info("Creating DynamoDB table {}", getTableName());
            waitForTableToBeReady();
        } catch (ResourceInUseException e) {
            // Table already exists
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

    public void createPoint(String name, double latitude, double longitude) {
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        // I don't know what this next line is for. Uniqueness?
        AttributeValue rangeKeyAttributeValue = new AttributeValue().withS(UUID.randomUUID().toString());
        AttributeValue foodbankNameAttributeValue = new AttributeValue().withS(name);

        PutPointRequest putPointRequest = new PutPointRequest(geoPoint, rangeKeyAttributeValue);
        putPointRequest.getPutItemRequest().addItemEntry(COLUMN_NAME, foodbankNameAttributeValue);

        PutPointResult putPointResult = geoDataManager.putPoint(putPointRequest);

        log.debug("Point: {}", putPointResult);
    }

    public List<Map<String, AttributeValue>> queryRadius(double latitude, double longitude, double rangeInMetres) {
        GeoPoint centerPoint = new GeoPoint(latitude, longitude);

        List<String> attributesToGet = new ArrayList<>();
        attributesToGet.add(COLUMN_RANGE_KEY);
        attributesToGet.add(COLUMN_GEO_JSON);
        attributesToGet.add(COLUMN_NAME);

        QueryRadiusRequest queryRadiusRequest = new QueryRadiusRequest(centerPoint, rangeInMetres);
        queryRadiusRequest.getQueryRequest().setAttributesToGet(attributesToGet);
        QueryRadiusResult queryRadiusResult = geoDataManager.queryRadius(queryRadiusRequest);

        log.debug("Query Radius search Result: {}", queryRadiusResult);
        return queryRadiusResult.getItem();
    }

}
