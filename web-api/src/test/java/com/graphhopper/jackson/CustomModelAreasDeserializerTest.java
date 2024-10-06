package com.graphhopper.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
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
        JsonFeatureCollection result = deserialize(validNode);

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
        invalidNode.put("type", faker.lorem().word());
        invalidNode.putArray("features").add(createRandomFeature());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> deserialize(invalidNode));

        String errorMessage = exception.getMessage();
        assertTrue(errorMessage.contains("Cannot construct instance of") ||
                        errorMessage.contains("no String-argument constructor/factory method") ||
                        errorMessage.contains("type") && errorMessage.contains("FeatureCollection"),
                "Exception message does not contain expected content. Actual message: "
                        + errorMessage);
    }

    // Intention : Tests that the deserializer will fail and throw an exception when passed a
    // JsonNode missing the type field.
    @Test
    public void testMissingType() {
        // Arrange
        ObjectNode noTypeNode = mapper.createObjectNode();
        noTypeNode.putArray("features").add(createRandomFeature());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> deserialize(noTypeNode));

        assertTrue(exception instanceof IllegalArgumentException ||
                        exception instanceof MismatchedInputException,
                "Expected either IllegalArgumentException or MismatchedInputException, " +
                        "but got: " + exception.getClass().getName());

        String errorMessage = exception.getMessage();
        assertTrue(errorMessage.contains("type") || errorMessage.contains("missing"),
                "Exception message should mention 'type' or 'missing'. Actual message: "
                        + errorMessage);
    }

    // Intention : Tests that the deserializer will fail and throw an exception if passed a null
    //             JsonNode.
    @Test
    public void testNullNode() {
        // Arrange
        JsonNode nullNode = null;

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> deserialize(nullNode));

        // Assert (continued)
        assertTrue(exception instanceof NullPointerException ||
                        exception instanceof IllegalArgumentException,
                "Expected either NullPointerException or IllegalArgumentException, " +
                        "but got: " + exception.getClass().getName());

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
        ObjectNode emptyNode = mapper.createObjectNode();

        // Act
        JsonFeatureCollection result = null;
        Exception exception = null;
        try {
            result = deserialize(emptyNode);
        } catch (Exception e) {
            exception = e;
        }

        // Assert
        if (exception != null) {

            // Assert (continued)
            String errorMessage = exception.getMessage();
            assertTrue(
                    errorMessage.contains("missing") ||
                            errorMessage.contains("empty") ||
                            errorMessage.contains("invalid") ||
                            (errorMessage.contains("type") &&
                                    errorMessage.contains("FeatureCollection")),
                    "Exception message does not contain expected content. Actual message: "
                            + errorMessage
            );
        } else {
            assertNotNull(result, "Expected a non-null result for empty node");
            assertTrue(result.getFeatures().isEmpty(), "Expected an empty feature " +
                    "collection for empty node");
        }
    }

    // Intention : Tests that the deserializer will fail and throw an exception if passed a JsonNode
    // without an "id" field.
    @Test
    public void testFeatureWithoutId() {
        // Arrange
        ObjectNode featureWithoutId = createRandomFeature();
        featureWithoutId.remove("id");
        ObjectNode validNode = mapper.createObjectNode();
        validNode.put("type", "FeatureCollection");
        validNode.putArray("features").add(featureWithoutId);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> deserialize(validNode));

        assertTrue(exception instanceof IllegalArgumentException ||
                        exception instanceof InvalidDefinitionException,
                "Expected either IllegalArgumentException or InvalidDefinitionException," +
                        " but got: " + exception.getClass().getName());

        String errorMessage = exception.getMessage();
        assertTrue(errorMessage.contains("id") || errorMessage.contains("JsonFeature"),
                "Exception message should mention 'id' or 'JsonFeature'. Actual message:" +
                        " " + errorMessage);
    }

    // Intention : Tests that the deserializer will fail and throw an exception if passed a GeoJSON
    // object with duplicate IDs.
    @Test
    public void testDuplicateFeatureIds() throws Exception {
        // Arrange
        String duplicateId = faker.internet().uuid();
        ObjectNode feature1 = createRandomFeature();
        ObjectNode feature2 = createRandomFeature();
        feature1.put("id", duplicateId);
        feature2.put("id", duplicateId);
        ObjectNode validNode = mapper.createObjectNode();
        validNode.put("type", "FeatureCollection");
        validNode.putArray("features").add(feature1).add(feature2);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> deserialize(validNode));

        assertTrue(exception instanceof IllegalArgumentException ||
                        exception instanceof InvalidDefinitionException,
                "Expected either IllegalArgumentException or InvalidDefinitionException," +
                        " but got: " + exception.getClass().getName());

        String errorMessage = exception.getMessage();
        assertTrue(errorMessage.contains("id") || errorMessage.contains("JsonFeature"),
                "Exception message should mention 'id' or 'JsonFeature'. Actual message:" +
                        " " + errorMessage);
    }


    private ObjectNode createRandomFeature() {
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

        return feature;
    }

    private JsonFeatureCollection deserialize(JsonNode node) throws IOException {
        return deserializer.deserialize(mapper.treeAsTokens(node), mapper.getDeserializationContext());
    }
}
