package org.foodbanksy.local;

import org.foodbanksy.serverless.ApiGatewayResponse;
import org.foodbanksy.serverless.handlers.ping.PingHandler;

import java.util.HashMap;

public class LocalDevelopment {
    public static void main(String[] args) {
        ApiGatewayResponse response = new PingHandler().handleRequest(new HashMap<>(), null);

        System.out.println(response);
    }
}
