package com.patred.openapi;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.patred.openapi.model.Format;
import com.patred.openapi.util.FormatUtils;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.converter.SwaggerConverter;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implementazione stabile e funzionante di OpenApiConverter. Converte specifiche Swagger 2 (OpenAPI
 * 2.0) in OpenAPI 3. Supporta JSON e YAML in input e output.
 */
public class OpenApiUpdaterImpl implements OpenApiUpdater {

  @Override
  public String convertToV3(String spec) throws Exception {
    Format format = FormatUtils.isYaml(spec) ? Format.YAML : Format.JSON;
    return convertToV3(spec, format);
  }

  @Override
  public String convertToV3(String spec,  Format format) throws Exception {

    // Scriviamo temporaneamente il contenuto su file perché SwaggerConverter lavora su path
    Path tempFile = Files.createTempFile("swagger2-", format == Format.YAML ? ".yaml" : ".json");
    Files.writeString(tempFile, spec);

    SwaggerConverter converter = new SwaggerConverter();
    SwaggerParseResult result = converter.readLocation(tempFile.toAbsolutePath().toString(), null,
        null);
    Files.deleteIfExists(tempFile);

    if (result == null || result.getOpenAPI() == null) {
      throw new IllegalArgumentException("Specifica Swagger 2 non valida o conversione fallita.");
    }

    OpenAPI openAPI = result.getOpenAPI();
    final ObjectMapper mapper = format == Format.YAML ? Yaml.mapper() : Json.mapper();
    mapper.setSerializationInclusion(Include.NON_NULL);

    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString((openAPI));
  }

}
