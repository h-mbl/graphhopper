/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.graphhopper.util;

import com.graphhopper.json.Statement;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static com.graphhopper.json.Statement.*;
import static com.graphhopper.json.Statement.Op.MULTIPLY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.github.javafaker.Faker;

public class CustomModelTest {

    @Test
    public void testMergeComparisonKeys() {
        CustomModel truck = new CustomModel();
        truck.addToPriority(If("max_width < 3", MULTIPLY, "0"));
        CustomModel car = new CustomModel();
        car.addToPriority(If("max_width<2", MULTIPLY, "0"));
        CustomModel bike = new CustomModel();
        bike.addToPriority(If("max_weight<0.02", MULTIPLY, "0"));

        assertEquals(2, CustomModel.merge(bike, car).getPriority().size());
        assertEquals(1, bike.getPriority().size());
        assertEquals(1, car.getPriority().size());
    }

    @Test
    public void testMergeElse() {
        CustomModel truck = new CustomModel();
        truck.addToPriority(If("max_width < 3", MULTIPLY, "0"));

        CustomModel car = new CustomModel();
        car.addToPriority(If("max_width < 2", MULTIPLY, "0"));

        CustomModel merged = CustomModel.merge(truck, car);
        assertEquals(2, merged.getPriority().size());
        assertEquals(1, car.getPriority().size());
    }

    @Test
    public void testMergeEmptyModel() {
        CustomModel emptyCar = new CustomModel();
        CustomModel car = new CustomModel();
        car.addToPriority(If("road_class==primary", MULTIPLY, "0.5"));
        car.addToPriority(ElseIf("road_class==tertiary", MULTIPLY, "0.8"));

        Iterator<Statement> iter = CustomModel.merge(emptyCar, car).getPriority().iterator();
        assertEquals("0.5", iter.next().value());
        assertEquals("0.8", iter.next().value());

        iter = CustomModel.merge(car, emptyCar).getPriority().iterator();
        assertEquals("0.5", iter.next().value());
        assertEquals("0.8", iter.next().value());
    }


    /*
    Intention: To verify that the addAreas() method of CustomModel correctly rejects areas with invalid IDs.
    This test ensures that:
        1. An IllegalArgumentException is thrown when trying to add an area with an invalid ID.
        2. The exception message contains the expected error description, including the invalid ID.
   */
    @Test
    void testAddAreasWithInvalidId() {

        // Arrange

        Faker faker = new Faker();
        CustomModel customModel = new CustomModel();
        JsonFeatureCollection externalAreas = new JsonFeatureCollection();

        String invalidId = faker.lorem().words(3).stream().reduce((a, b) -> a + " " + b).orElse("invalid id");
        JsonFeature invalidFeature = new JsonFeature();
        invalidFeature.setId(invalidId);
        externalAreas.getFeatures().add(invalidFeature);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customModel.addAreas(externalAreas);
        });


        String expectedMessage = "The area '" + invalidId + "' has an invalid id. Only letters, numbers and underscore are allowed.";
        String actualMessage = exception.getMessage();

        // Assert (continued)
        assertTrue(actualMessage.contains(expectedMessage),
                "Expected message to contain: " + expectedMessage + ", but was: " + actualMessage);
    }
}
