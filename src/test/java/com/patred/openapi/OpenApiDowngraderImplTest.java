package com.patred.openapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.patred.openapi.model.Format;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class OpenApiDowngraderImplTest {

  private final OpenApiDowngraderImpl converter = new OpenApiDowngraderImpl();

  private final String openApi3Json = """
      {
        "openapi": "3.0.1",
        "info": {
          "title": "Pet API",
          "version": "1.0.0"
        },
        "servers": [
          { "url": "https://api.example.com/v1" }
        ],
        "paths": {
          "/pets": {
            "get": {
              "summary": "List pets",
              "operationId": "listPets",
              "responses": {
                "200": {
                  "description": "successful operation",
                  "content": {
                    "application/json": {
                      "schema": {
                        "type": "array",
                        "items": { "$ref": "#/components/schemas/Pet" }
                      }
                    }
                  }
                }
              }
            },
            "post": {
              "summary": "Create a pet",
              "operationId": "createPet",
              "requestBody": {
                "description": "Pet to add to the store",
                "content": {
                  "application/json": {
                    "schema": { "$ref": "#/components/schemas/Pet" }
                  }
                }
              },
              "responses": {
                "201": {
                  "description": "created",
                  "content": {
                    "application/json": {
                      "schema": { "$ref": "#/components/schemas/Pet" }
                    }
                  }
                }
              }
            }
          }
        },
        "components": {
          "schemas": {
            "Pet": {
              "type": "object",
              "properties": {
                "id": { "type": "integer" },
                "name": { "type": "string" }
              }
            }
          },
          "securitySchemes": {
            "api_key": {
              "type": "apiKey",
              "name": "api_key",
              "in": "header"
            }
          }
        }
      }
      """;

  @Test
  void testConvertToSwaggerAsInputFormat() throws Exception {
    String swagger2 = converter.convertToV2(openApi3Json);

    assertNotNull(swagger2);
    assertTrue(swagger2.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim().startsWith("{ \"swagger\""));
    assertTrue(swagger2.contains("\"swagger\""));
    assertTrue(swagger2.contains("\"2.0\""));
    assertTrue(swagger2.contains("\"info\""));
    assertTrue(swagger2.contains("\"paths\""));
    assertTrue(swagger2.contains("\"definitions\""));
    assertTrue(swagger2.contains("\"securityDefinitions\""));
    assertTrue(swagger2.contains("\"parameters\""));
    assertTrue(swagger2.contains("\"responses\""));
  }

  @Test
  void testConvertJsonToSwagger2Json() throws Exception {
    String swagger2 = converter.convertToV2(openApi3Json, Format.JSON);

    assertNotNull(swagger2);
    assertTrue(swagger2.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim().startsWith("{ \"swagger\""));
    assertTrue(swagger2.contains("\"swagger\""));
    assertTrue(swagger2.contains("\"2.0\""));
    assertTrue(swagger2.contains("\"info\""));
    assertTrue(swagger2.contains("\"paths\""));
    assertTrue(swagger2.contains("\"definitions\""));
    assertTrue(swagger2.contains("\"securityDefinitions\""));
    assertTrue(swagger2.contains("\"parameters\""));
    assertTrue(swagger2.contains("\"responses\""));
  }

  @Test
  void testConvertJsonToSwagger2Yaml() throws Exception {
    String swagger2Yaml = converter.convertToV2(openApi3Json, Format.YAML);
    assertTrue(swagger2Yaml.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim().startsWith("--- swagger"));
    assertNotNull(swagger2Yaml);
    assertTrue(swagger2Yaml.contains("swagger: \"2.0\""));
    assertTrue(swagger2Yaml.contains("info:"));
    assertTrue(swagger2Yaml.contains("paths:"));
    assertTrue(swagger2Yaml.contains("definitions:"));
  }

  @Test
  void testMissingOpenApiField() {
    String invalid = "{ \"info\": { \"title\": \"No OpenAPI\" } }";
    Exception ex = assertThrows(IllegalArgumentException.class,
        () -> converter.convertToV2(invalid, Format.JSON));
    assertEquals("Specifica non valida: manca il campo 'openapi'", ex.getMessage());
  }

  @Test
  void testYamlInput() throws Exception {
    String openApiYaml = """
        openapi: 3.0.1
        info:
          title: Sample API
          version: 1.0.0
        servers:
          - url: https://example.org/api
        paths:
          /test:
            get:
              summary: Test op
              responses:
                "200":
                  description: ok
        """;

    String swagger = converter.convertToV2(openApiYaml);
    assertTrue(swagger.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim().startsWith("--- swagger"));
    assertTrue(swagger.contains("swagger: \"2.0\""));
  }

  @Test
  void testYamlInputAndOutputJson() throws Exception {
    String openApiYaml = """
        openapi: 3.0.1
        info:
          title: Sample API
          version: 1.0.0
        servers:
          - url: https://example.org/api
        paths:
          /test:
            get:
              summary: Test op
              responses:
                "200":
                  description: ok
        """;

    String swagger = converter.convertToV2(openApiYaml, Format.JSON);
    assertTrue(swagger.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim().startsWith("{ \"swagger\""));
    assertTrue(swagger.contains("\"swagger\""));
    assertTrue(swagger.contains("\"2.0\""));
  }

  @Test
  void testReadFromFile() throws Exception {
    final String openApiYaml = new String(Files.readAllBytes(
        Path.of("src/test/resources/sample-openapi3.yaml")));

    String swagger = converter.convertToV2(openApiYaml, Format.JSON);
    assertTrue(swagger.replaceAll("[\\r\\n]+", " ").replaceAll(" +", " ").trim().startsWith("{ \"swagger\""));
    assertTrue(swagger.contains("\"swagger\""));
    assertTrue(swagger.contains("\"2.0\""));
  }

}
