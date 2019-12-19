package org.foodbanksy.dynamodb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DynamoDbExtension.class)
public class DynamoDbTest {

    @Test
    public void test() {
        System.out.println("OK, so I didn't actually write any tests that used the local dynamodb :-(");
    }
}
