package com.patred.openapi;

/**
 * Converter da Swagger 2 (OpenAPI 2.0) a OpenAPI 3.x
 */
public interface OpenApiConverter {

  /**
   * Converte una specifica OpenAPI 2 (Swagger) in OpenAPI 3. Mantiene il formato dell’input (JSON o
   * YAML).
   *
   * @param spec Contenuto della specifica (JSON o YAML)
   * @return Specifica convertita in OpenAPI 3, nello stesso formato dell’input
   * @throws Exception se la conversione fallisce
   */
  String convertToV3(String spec) throws Exception;

  /**
   * Converte una specifica OpenAPI 2 (Swagger) in OpenAPI 3, forzando l’output.
   *
   * @param spec       Contenuto della specifica (JSON o YAML)
   * @param outputYaml true per forzare YAML, false per forzare JSON
   * @return Specifica convertita in OpenAPI 3, nel formato richiesto
   * @throws Exception se la conversione fallisce
   */
  String convertToV3(String spec, boolean outputYaml) throws Exception;




}
