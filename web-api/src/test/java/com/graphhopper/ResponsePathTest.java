package com.graphhopper;

import com.graphhopper.util.PointList;
import com.graphhopper.util.details.PathDetail;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.github.javafaker.Faker;
import org.locationtech.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ResponsePathTest {

    /*
    Intentions: To ensure that the calcBBox2D() method correctly calculates the 2D bounding box for a given set of points.
    It tests the following cases:
        * The bounding box is not null.
        * The minimum coordinates are less than or equal to the maximum coordinates (which is logical for a bounding box).
        * All added points are effectively contained within the calculated bounding box.
    */
    @Test
    void testCalcBBox2D() {
        // Arrange
        Faker faker = new Faker();
        ResponsePath responsePath = new ResponsePath();
        PointList pointList = new PointList();

        // Ajout de points aléatoires au pointList
        for (int i = 0; i < 10; i++) {
            double lat = faker.number().randomDouble(6, -90, 90);
            double lon = faker.number().randomDouble(6, -180, 180);
            pointList.add(lat, lon);
        }

        // Act
        responsePath.setPoints(pointList);

        Envelope bbox = responsePath.calcBBox2D();

        // Asserts
        assertNotNull(bbox);
        assertTrue(bbox.getMinX() <= bbox.getMaxX());
        assertTrue(bbox.getMinY() <= bbox.getMaxY());

        // Vérifier que tous les points sont dans la boîte englobante
        for (int i = 0; i < pointList.size(); i++) {
            assertTrue(bbox.contains(pointList.getLon(i), pointList.getLat(i)));
        }
    }

    /*
    Intention: To verify that the addPathDetails() method  adds path details to a ResponsePath object
    This test ensures:
        1. Path details are correctly added to an empty ResponsePath.
        2. The size of added details matches the size of the original map.
        3. An exception is thrown when trying to add details of a different size.
   */
    @Test
    void testAddPathDetails() {

        //Arrange
        ResponsePath responsePath = new ResponsePath();

        Map<String, List<PathDetail>> details1 = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            String key = "key" + i;
            List<PathDetail> pathDetails = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                Object value = Math.random() * 100;
                pathDetails.add(new PathDetail(value));
            }
            details1.put(key, pathDetails);
        }

        // Act
        responsePath.addPathDetails(details1);

        //Assert
        assertEquals(details1.size(), responsePath.getPathDetails().size());


        // Arrange (for exception test)
        Map<String, List<PathDetail>> details3 = new HashMap<>();
        for (int i = 0; i < 2; i++) {
            String key = "key" + (i + 3); // Utilisation de nouvelles clés
            List<PathDetail> pathDetails = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                Object value = Math.random() * 100;
                pathDetails.add(new PathDetail(value));
            }
            details3.put(key, pathDetails);
        }

        // Act & Assert (for exception test)
        assertThrows(IllegalStateException.class, () -> responsePath.addPathDetails(details3));
    }

}
