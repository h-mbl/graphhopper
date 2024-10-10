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

    @Test
    void testCalcBBox2D() {
        Faker faker = new Faker();
        ResponsePath responsePath = new ResponsePath();
        PointList pointList = new PointList();

        // Add random points to the pointList
        for (int i = 0; i < 10; i++) {
            double lat = faker.number().randomDouble(6, -90, 90);
            double lon = faker.number().randomDouble(6, -180, 180);
            pointList.add(lat, lon);
        }

        responsePath.setPoints(pointList);

        Envelope bbox = responsePath.calcBBox2D();

        assertNotNull(bbox);
        assertTrue(bbox.getMinX() <= bbox.getMaxX());
        assertTrue(bbox.getMinY() <= bbox.getMaxY());

        // Check if all points are within the bounding box
        for (int i = 0; i < pointList.size(); i++) {
            assertTrue(bbox.contains(pointList.getLon(i), pointList.getLat(i)));
        }
    }
    @Test
    void testAddPathDetails() {
        ResponsePath responsePath = new ResponsePath();

        //  Adding details to an empty pathDetails
        Map<String, List<PathDetail>> details1 = createRandomPathDetails(3, 5);
        responsePath.addPathDetails(details1);
        assertEquals(details1.size(), responsePath.getPathDetails().size());

        // Adding details with different size (should throw exception)
        Map<String, List<PathDetail>> details3 = createRandomPathDetails(2, 5);
        assertThrows(IllegalStateException.class, () -> responsePath.addPathDetails(details3));
    }
        private Map<String, List<PathDetail>> createRandomPathDetails(int numKeys, int numDetailsPerKey) {
        Faker faker = new Faker();
        Map<String, List<PathDetail>> details = new HashMap<>();
        for (int i = 0; i < numKeys; i++) {
            String key = faker.lorem().word();
            List<PathDetail> pathDetails = new ArrayList<>();
            for (int j = 0; j < numDetailsPerKey; j++) {
                Object value = faker.number().randomDouble(2, 0, 100);
                pathDetails.add(new PathDetail(value));
            }
            details.put(key, pathDetails);
        }
        return details;
    }
}
