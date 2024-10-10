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

        // Ajout de points aléatoires au pointList
        for (int i = 0; i < 10; i++) {
            double lat = faker.number().randomDouble(6, -90, 90);
            double lon = faker.number().randomDouble(6, -180, 180);
            pointList.add(lat, lon);
        }

        responsePath.setPoints(pointList);

        Envelope bbox = responsePath.calcBBox2D();

        // Assertions
        assertNotNull(bbox);
        assertTrue(bbox.getMinX() <= bbox.getMaxX());
        assertTrue(bbox.getMinY() <= bbox.getMaxY());

        // Vérifier que tous les points sont dans la boîte englobante
        for (int i = 0; i < pointList.size(); i++) {
            assertTrue(bbox.contains(pointList.getLon(i), pointList.getLat(i)));
        }
    }

    @Test
    void testAddPathDetails() {
        ResponsePath responsePath = new ResponsePath();

        // Création de détails aléatoires pour pathDetails
        Map<String, List<PathDetail>> details1 = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            String key = "key" + i; // Utilisation d'une clé simple
            List<PathDetail> pathDetails = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                // Ajout d'un PathDetail avec une valeur aléatoire
                Object value = Math.random() * 100; // Utilisation de Math.random() pour une valeur aléatoire
                pathDetails.add(new PathDetail(value));
            }
            details1.put(key, pathDetails);
        }

        // Ajout des détails à une responsePath vide
        responsePath.addPathDetails(details1);
        assertEquals(details1.size(), responsePath.getPathDetails().size(), "Le nombre de détails ajoutés ne correspond pas.");

        // Ajout de détails de taille différente (devrait lever une exception)
        Map<String, List<PathDetail>> details3 = new HashMap<>();
        for (int i = 0; i < 2; i++) {
            String key = "key" + (i + 3); // Utilisation de nouvelles clés
            List<PathDetail> pathDetails = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                Object value = Math.random() * 100; // Utilisation de Math.random() pour une valeur aléatoire
                pathDetails.add(new PathDetail(value));
            }
            details3.put(key, pathDetails);
        }

        // Vérification que l'exception est levée
        assertThrows(IllegalStateException.class, () -> responsePath.addPathDetails(details3));
    }

}
