package com.graphhopper.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.graphhopper.util.JsonFeature;
import com.graphhopper.util.JsonFeatureCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CustomModelAreasDeserializerTest {
    private ObjectMapper mapper;
    private CustomModelAreasDeserializer deserializer;

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
        deserializer = new CustomModelAreasDeserializer();
    }

    @Test
    public void testValidFeatureCollection() throws Exception {
        JsonNode validNode = mapper.readTree("{\"type\": \"FeatureCollection\", \"features\": []}");
        assertTrue(isFeatureCollection(validNode), "Should identify valid FeatureCollection");
    }

    @Test
    public void testInvalidType() throws Exception {
        JsonNode invalidNode = mapper.readTree("{\"type\": \"OtherType\", \"features\": []}");
        assertFalse(isFeatureCollection(invalidNode), "Should reject non-FeatureCollection type");
    }

    @Test
    public void testMissingType() throws Exception {
        JsonNode noTypeNode = mapper.readTree("{\"features\": []}");
        assertFalse(isFeatureCollection(noTypeNode), "Should reject node without type");
    }

    @Test
    public void testNullNode() {
        assertFalse(isFeatureCollection(null), "Should handle null node");
    }

    @Test
    public void testEmptyNode() {
        ObjectNode emptyNode = mapper.createObjectNode();
        assertFalse(isFeatureCollection(emptyNode), "Should handle empty node");
    }

    private boolean isFeatureCollection(JsonNode treeNode) {
        try {
            if (treeNode == null) return false;
            JsonParser parser = mapper.treeAsTokens(treeNode);
            DeserializationContext context = mapper.getDeserializationContext();
            JsonFeatureCollection result = deserializer.deserialize(parser, context);
            return result != null && "FeatureCollection".equals(treeNode.path("type").asText());
        } catch (IOException e) {
            return false;
        }
    }
}
