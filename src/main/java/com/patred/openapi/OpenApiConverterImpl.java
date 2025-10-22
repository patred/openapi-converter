package com.patred.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.converter.SwaggerConverter;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implementazione stabile e funzionante di OpenApiConverter.
 * Converte specifiche Swagger 2 (OpenAPI 2.0) in OpenAPI 3.
 * Supporta JSON e YAML in input e output.
 */
public class OpenApiConverterImpl implements OpenApiConverter {

  private final ObjectMapper jsonMapper = new ObjectMapper();
  private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

  @Override
  public String convertToV3(String spec) throws Exception {
    boolean isYaml = isYaml(spec);
    return convertToV3(spec, isYaml);
  }

  @Override
  public String convertToV3(String spec, boolean outputYaml) throws Exception {
    // Scriviamo temporaneamente il contenuto su file perché SwaggerConverter lavora su path
    Path tempFile = Files.createTempFile("swagger2-", outputYaml ? ".yaml" : ".json");
    Files.writeString(tempFile, spec);

    SwaggerConverter converter = new SwaggerConverter();
    SwaggerParseResult result = converter.readLocation(tempFile.toAbsolutePath().toString(), null, null);
    Files.deleteIfExists(tempFile);

    if (result == null || result.getOpenAPI() == null) {
      throw new IllegalArgumentException("Specifica Swagger 2 non valida o conversione fallita.");
    }

    OpenAPI openAPI = result.getOpenAPI();
    ObjectMapper mapper = outputYaml ? yamlMapper : jsonMapper;
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(openAPI);
  }

  /** Rileva se la stringa è YAML (anziché JSON) */
  private boolean isYaml(String spec) {
    String trimmed = spec.trim();
    return !(trimmed.startsWith("{") || trimmed.startsWith("["));
  }
}
