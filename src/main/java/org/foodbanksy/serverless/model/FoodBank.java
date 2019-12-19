package org.foodbanksy.serverless.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class FoodBank {
    private String name;
    private double latitude;
    private double longitude;
}
