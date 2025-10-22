package com.patred.openapi;

/**
 * Converter da Swagger 2 (OpenAPI 2.0) a OpenAPI 3.x
 */
public interface OpenApiDowngrader {

  /**
   * Converte una specifica OpenAPI 3 in OpenAPI 2 (Swagger). Mantiene il formato dell’input (JSON o
   * YAML).
   *
   * @param spec Contenuto della specifica (JSON o YAML)
   * @return Specifica convertita in 2 (Swagger), nello stesso formato dell’input
   * @throws Exception se la conversione fallisce
   */
  String convertToV2(String spec) throws Exception;

  /**
   * Converte una specifica OpenAPI 3 in OpenAPI 2 (Swagger), forzando l’output.
   *
   * @param spec       Contenuto della specifica (JSON o YAML)
   * @param outputYaml true per forzare YAML, false per forzare JSON
   * @return Specifica convertita in 2 (Swagger), nel formato richiesto
   * @throws Exception se la conversione fallisce
   */
  String convertToV2(String spec, boolean outputYaml) throws Exception;
}
