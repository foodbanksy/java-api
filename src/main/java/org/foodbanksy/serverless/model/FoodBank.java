package org.foodbanksy.serverless.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FoodBank {
    private String name;
    private double latitude;
    private double longitude;
}
