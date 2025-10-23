package com.patred.openapi.util;

/**
 * Utility class per rilevare il formato di una specifica OpenAPI.
 */
public final class FormatUtils {

  // Costruttore privato per impedire l'instanziazione
  private FormatUtils() {
  }

  /**
   * Determina se una stringa rappresenta YAML.
   *
   * @param spec La specifica OpenAPI come stringa
   * @return true se la stringa sembra YAML, false se JSON
   */
  public static boolean isYaml(String spec) {
    if (spec == null || spec.isBlank()) {
      return false;
    }
    String trimmed = spec.trim();
    return !(trimmed.startsWith("{") || trimmed.startsWith("["));
  }
}
