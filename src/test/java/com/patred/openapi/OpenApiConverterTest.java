package com.patred.openapi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OpenApiConverterTest {

  private final OpenApiConverter converter = new OpenApiConverterImpl();

  private static final String SAMPLE_YAML = """
        swagger: "2.0"
        info:
          title: Sample API
          version: "1.0.0"
        paths:
          /hello:
            get:
              responses:
                200:
                  description: Success
        """;

  private static final String SAMPLE_JSON = """
        {
          "swagger": "2.0",
          "info": {
            "title": "Sample API",
            "version": "1.0.0"
          },
          "paths": {
            "/hello": {
              "get": {
                "responses": {
                  "200": {
                    "description": "Success"
                  }
                }
              }
            }
          }
        }
        """;

  @Test
  void testConvertYamlToOpenApi3() throws Exception {
    String result = converter.convertToV3(SAMPLE_YAML);
    assertNotNull(result);
    assertTrue(result.contains("openapi"), "Output deve contenere 'openapi'");
    assertTrue(result.contains("/hello"));
  }

  @Test
  void testConvertJsonToOpenApi3() throws Exception {
    String result = converter.convertToV3(SAMPLE_JSON);
    assertNotNull(result);
    assertTrue(result.contains("\"openapi\""), "Output JSON deve contenere campo 'openapi'");
  }

  @Test
  void testForceYamlOutput() throws Exception {
    String result = converter.convertToV3(SAMPLE_JSON, true);
    System.out.println(result);
    assertTrue(result.trim().startsWith("---\nopenapi:"), "Output deve essere YAML");
  }

  @Test
  void testForceJsonOutput() throws Exception {
    String result = converter.convertToV3(SAMPLE_YAML, false);
    assertTrue(result.trim().startsWith("{"), "Output deve essere JSON");
  }

  @Test
  void testInvalidInputThrows() {
    String invalid = "swagger: wrong: spec";
    Exception ex = assertThrows(IllegalArgumentException.class, () -> {
      converter.convertToV3(invalid);
    });
    assertTrue(ex.getMessage().toLowerCase().contains("non valida")
        || ex.getMessage().toLowerCase().contains("fallita"));
  }
}
