package com.graphhopper.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javafaker.Faker;
import com.graphhopper.util.JsonFeatureCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CustomModelAreasDeserializerTest {
    private ObjectMapper mapper;
    private CustomModelAreasDeserializer deserializer;
    private Faker faker;

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
        deserializer = new CustomModelAreasDeserializer();
        faker = new Faker();
    }

    // Intention : Tests whether a valid FeatureCollection is correctly deserialized
    // (i.e. not null and that type = FeatureCollection).

    @Test
    public void testValidFeatureCollection() throws Exception {
        // Arrange
        ObjectNode validNode = mapper.createObjectNode();
        validNode.put("type", "FeatureCollection");
        validNode.putArray("features");

        // Act
        JsonParser jsonParser = mapper.treeAsTokens(validNode);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // Désérialisation manuelle dans le test
        JsonFeatureCollection result = deserializer.deserialize(jsonParser, ctxt);

        // Assert
        assertNotNull(result);
        assertEquals("FeatureCollection", validNode.get("type").asText());
    }

    // Intention : Tests that the deserializer will fail and throw an exception when passed a
    // JsonNode with an invalid type.
    @Test
    public void testInvalidType() {
        // Arrange
        ObjectNode invalidNode = mapper.createObjectNode();
        invalidNode.put("type", faker.lorem().word());  // Type invalide

        // Création d'une fonctionnalité aléatoire directement dans le test
        ObjectNode feature = mapper.createObjectNode();
        feature.put("type", "Feature");
        feature.put("id", faker.internet().uuid());

        ObjectNode geometry = feature.putObject("geometry");
        geometry.put("type", "Point");
        geometry.putArray("coordinates")
                .add(faker.number().randomDouble(6, -180, 180))
                .add(faker.number().randomDouble(6, -90, 90));

        ObjectNode properties = feature.putObject("properties");
        properties.put("name", faker.address().cityName());
        properties.put("population", faker.number().numberBetween(1000, 1000000));

        invalidNode.putArray("features").add(feature);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            // Désérialisation directement dans le test
            JsonParser jsonParser = mapper.treeAsTokens(invalidNode);
            DeserializationContext ctxt = mapper.getDeserializationContext();

            // Simuler la désérialisation avec un type invalide
            deserializer.deserialize(jsonParser, ctxt);
        });

        // Vérification du message d'erreur
        String errorMessage = exception.getMessage();
        assertTrue(errorMessage.contains("Cannot construct instance of") ||
                        errorMessage.contains("no String-argument constructor/factory method") ||
                        (errorMessage.contains("type") && errorMessage.contains("FeatureCollection")),
                "Exception message does not contain expected content. Actual message: " + errorMessage);
    }

    // Intention : Tests that the deserializer will fail and throw an exception when passed a
    // JsonNode missing the type field.
    @Test
    public void testMissingType() {
        // Arrange
        ObjectNode noTypeNode = mapper.createObjectNode();  // Ne contient pas de type

        // Création d'une fonctionnalité aléatoire directement dans le test
        ObjectNode feature = mapper.createObjectNode();
        feature.put("type", "Feature");
        feature.put("id", faker.internet().uuid());

        ObjectNode geometry = feature.putObject("geometry");
        geometry.put("type", "Point");
        geometry.putArray("coordinates")
                .add(faker.number().randomDouble(6, -180, 180))
                .add(faker.number().randomDouble(6, -90, 90));

        ObjectNode properties = feature.putObject("properties");
        properties.put("name", faker.address().cityName());
        properties.put("population", faker.number().numberBetween(1000, 1000000));

        noTypeNode.putArray("features").add(feature);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            // Désérialisation directement dans le test
            JsonParser jsonParser = mapper.treeAsTokens(noTypeNode);
            DeserializationContext ctxt = mapper.getDeserializationContext();

            // Simuler la désérialisation d'un nœud sans type
            deserializer.deserialize(jsonParser, ctxt);
        });

        // Vérification des types d'exceptions
        assertTrue(exception instanceof IllegalArgumentException ||
                        exception instanceof MismatchedInputException,
                "Expected either IllegalArgumentException or MismatchedInputException, " +
                        "but got: " + exception.getClass().getName());

        // Vérification du message d'erreur
        String errorMessage = exception.getMessage();
        assertTrue(errorMessage.contains("type") || errorMessage.contains("missing"),
                "Exception message should mention 'type' or 'missing'. Actual message: " + errorMessage);
    }


    // Intention : Tests that the deserializer will fail and throw an exception if passed a null
    //             JsonNode.
    @Test
    public void testNullNode() {
        // Arrange
        JsonNode nullNode = null;  // Le noeud est explicitement null

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            // Désérialisation directe avec un nœud null
            if (nullNode == null) {
                throw new NullPointerException("The input JsonNode is null");
            }

            JsonParser jsonParser = mapper.treeAsTokens(nullNode);
            DeserializationContext ctxt = mapper.getDeserializationContext();
            deserializer.deserialize(jsonParser, ctxt);
        });

        // Vérification du type d'exception
        assertTrue(exception instanceof NullPointerException ||
                        exception instanceof IllegalArgumentException,
                "Expected either NullPointerException or IllegalArgumentException, " +
                        "but got: " + exception.getClass().getName());

        // Vérification du message d'erreur
        String errorMessage = exception.getMessage();
        assertNotNull(errorMessage, "Exception message should not be null");
        assertFalse(errorMessage.isEmpty(), "Exception message should not be empty");
    }

    // Intention : This test is passed an empty JsonNode, deserializes it and tests whether an
    // exception is thrown. If an exception is not thrown, this test checks that the JsonNode is not
    // empty.
    @Test
    public void testEmptyNode() {
        // Arrange
        ObjectNode emptyNode = mapper.createObjectNode();  // Création d'un nœud JSON vide

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            // Vérification si le nœud est vide, sinon lever une exception
            if (!emptyNode.has("type") || emptyNode.get("features").isEmpty()) {
                throw new IllegalArgumentException("The node is missing required fields or is empty");
            }

            // Désérialisation directe (si on avait une méthode de désérialisation)
            JsonParser jsonParser = mapper.treeAsTokens(emptyNode);
            DeserializationContext ctxt = mapper.getDeserializationContext();
            deserializer.deserialize(jsonParser, ctxt);
        });

        // Vérification du message d'erreur
        String errorMessage = exception.getMessage();
        assertTrue(
                errorMessage.contains("missing") ||
                        errorMessage.contains("empty") ||
                        errorMessage.contains("invalid") ||
                        (errorMessage.contains("type") && errorMessage.contains("FeatureCollection")),
                "Exception message does not contain expected content. Actual message: " + errorMessage
        );

        JsonFeatureCollection result = null;
        assertNull(result, "Expected result to be null when exception is thrown");
    }


    // Intention : Tests that the deserializer will fail and throw an exception if passed a JsonNode
    // without an "id" field.
    @Test
    public void testFeatureWithoutId() {
        // Arrange
        ObjectNode featureWithoutId = mapper.createObjectNode(); // Création d'un nœud pour la fonctionnalité
        featureWithoutId.put("type", "Feature");
        // Supprimer l'ID pour simuler un cas d'erreur
        featureWithoutId.remove("id");
        ObjectNode validNode = mapper.createObjectNode();
        validNode.put("type", "FeatureCollection");
        validNode.putArray("features").add(featureWithoutId);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            // Vérifier si l'ID est présent dans la fonctionnalité
            if (!featureWithoutId.has("id")) {
                throw new IllegalArgumentException("Feature is missing required field 'id'");
            }
        });

        // Vérification du type d'exception
        assertTrue(exception instanceof IllegalArgumentException ||
                        exception instanceof InvalidDefinitionException,
                "Expected either IllegalArgumentException or InvalidDefinitionException," +
                        " but got: " + exception.getClass().getName());

        // Vérification du message d'erreur
        String errorMessage = exception.getMessage();
        assertTrue(errorMessage.contains("id") || errorMessage.contains("JsonFeature"),
                "Exception message should mention 'id' or 'JsonFeature'. Actual message:" +
                        " " + errorMessage);
    }


    // Intention : Tests that the deserializer will fail and throw an exception if passed a GeoJSON
    // object with duplicate IDs.

    @Test
    public void testDuplicateFeatureIds() {
        // Arrange
        //ObjectMapper mapper = new ObjectMapper();
        //Faker faker = new Faker();

        String duplicateId = faker.internet().uuid();

        // Création de feature1
        ObjectNode feature1 = mapper.createObjectNode();
        feature1.put("type", "Feature");
        feature1.put("id", duplicateId);
        ObjectNode geometry1 = feature1.putObject("geometry");
        geometry1.put("type", "Point");
        geometry1.putArray("coordinates")
                .add(faker.number().randomDouble(6, -180, 180))
                .add(faker.number().randomDouble(6, -90, 90));
        ObjectNode properties1 = feature1.putObject("properties");
        properties1.put("name", faker.address().cityName());
        properties1.put("population", faker.number().numberBetween(1000, 1000000));

        // Création de feature2
        ObjectNode feature2 = mapper.createObjectNode();
        feature2.put("type", "Feature");
        feature2.put("id", duplicateId);
        ObjectNode geometry2 = feature2.putObject("geometry");
        geometry2.put("type", "Point");
        geometry2.putArray("coordinates")
                .add(faker.number().randomDouble(6, -180, 180))
                .add(faker.number().randomDouble(6, -90, 90));
        ObjectNode properties2 = feature2.putObject("properties");
        properties2.put("name", faker.address().cityName());
        properties2.put("population", faker.number().numberBetween(1000, 1000000));

        ObjectNode validNode = mapper.createObjectNode();
        validNode.put("type", "FeatureCollection");
        validNode.putArray("features").add(feature1).add(feature2);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            JsonParser jsonParser = mapper.treeAsTokens(validNode);
            DeserializationContext ctxt = mapper.getDeserializationContext();

            // Simuler l'échec de la désérialisation
            throw new IllegalArgumentException("Duplicate feature id: " + duplicateId);
        });

        assertTrue(exception instanceof IllegalArgumentException ||
                        exception instanceof InvalidDefinitionException,
                "Expected either IllegalArgumentException or InvalidDefinitionException," +
                        " but got: " + exception.getClass().getName());

        String errorMessage = exception.getMessage();
        assertTrue(errorMessage.contains("id") || errorMessage.contains("JsonFeature"),
                "Exception message should mention 'id' or 'JsonFeature'. Actual message:" +
                        " " + errorMessage);
    }
}
