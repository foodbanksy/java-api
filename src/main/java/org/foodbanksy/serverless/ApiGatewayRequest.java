package org.foodbanksy.serverless;

import lombok.Data;

import java.util.Map;

@Data
public class ApiGatewayRequest {
    private String resource;
    private String path;
    private String httpMethod;
    private Map<String, String> headers;
    private Map<String, String> queryStringParameters;
    private Map<String, String> pathParameters;
    private Map<String, String> stageVariables;
    private String body;
    private boolean isBase64Encoded;
}