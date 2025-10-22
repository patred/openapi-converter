package com.patred.openapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

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
  @DisplayName("Converte OpenAPI 3 JSON in Swagger 2 JSON")
  void testConvertJsonToSwagger2Json() throws Exception {
    String swagger2 = converter.convertToV2(openApi3Json, false);

    assertNotNull(swagger2);
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
  @DisplayName("Converte OpenAPI 3 JSON in Swagger 2 YAML")
  void testConvertJsonToSwagger2Yaml() throws Exception {
    String swagger2Yaml = converter.convertToV2(openApi3Json, true);

    assertNotNull(swagger2Yaml);
    assertTrue(swagger2Yaml.contains("swagger: \"2.0\""));
    assertTrue(swagger2Yaml.contains("info:"));
    assertTrue(swagger2Yaml.contains("paths:"));
    assertTrue(swagger2Yaml.contains("definitions:"));
  }

  @Test
  @DisplayName("Gestisce errore se manca il campo openapi")
  void testMissingOpenApiField() {
    String invalid = "{ \"info\": { \"title\": \"No OpenAPI\" } }";
    Exception ex = assertThrows(IllegalArgumentException.class, () -> converter.convertToV2(invalid, false));
    assertEquals("Specifica non valida: manca il campo 'openapi'", ex.getMessage());
  }

  @Test
  @DisplayName("Supporta input YAML")
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

    String swagger2 = converter.convertToV2(openApiYaml, false);
    assertTrue(swagger2.contains("\"swagger\""));
    assertTrue(swagger2.contains("\"2.0\""));
  }
}
