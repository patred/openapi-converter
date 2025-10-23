package com.patred.openapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.patred.openapi.model.Format;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class OpenApiUpdaterTest {

  private final OpenApiUpdater converter = new OpenApiUpdaterImpl();

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
    assertTrue(
        result.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim().startsWith("openapi:"));
    assertTrue(result.contains("openapi"), "Output deve contenere 'openapi'");
    assertTrue(result.contains("/hello"));
  }

  @Test
  void testConvertJsonToOpenApi3() throws Exception {
    String result = converter.convertToV3(SAMPLE_JSON);
    assertNotNull(result);
    assertTrue(result.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim()
        .startsWith("{ \"openapi\""));
    assertTrue(result.contains("\"openapi\""), "Output JSON deve contenere campo 'openapi'");
  }

  @Test
  void testForceYamlOutput() throws Exception {
    String result = converter.convertToV3(SAMPLE_JSON, Format.YAML);
    assertNotNull(result);
    assertTrue(
        result.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim().startsWith("openapi:"));
    assertTrue(result.contains("openapi:"), "Output deve essere YAML");
  }

  @Test
  void testForceJsonOutput() throws Exception {
    String result = converter.convertToV3(SAMPLE_YAML, Format.JSON);
    assertNotNull(result);
    assertTrue(result.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim()
        .startsWith("{ \"openapi\""));
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

  @Test
  void testReadFromFile() throws Exception {
    final String swaggerYaml = new String(Files.readAllBytes(
        Path.of("src/test/resources/sample-swagger2.yaml")));

    String result = converter.convertToV3(swaggerYaml);
    assertNotNull(result);
    assertTrue(result.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim()
        .startsWith("{ \"openapi\""));
    assertTrue(result.contains("openapi"), "Output deve contenere 'openapi'");
    System.out.println(result);
  }

  @Test
  void testReadFromFileForceJson() throws Exception {
    final String swaggerYaml = new String(Files.readAllBytes(
        Path.of("src/test/resources/sample-swagger2.yaml")));

    String result = converter.convertToV3(swaggerYaml, Format.JSON);
    assertNotNull(result);
    assertTrue(result.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim()
        .startsWith("{ \"openapi\""));
    assertTrue(result.contains("openapi"), "Output deve contenere 'openapi'");
    System.out.println(result);
  }
}
