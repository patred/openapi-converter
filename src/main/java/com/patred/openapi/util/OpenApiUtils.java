package com.patred.openapi.util;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.Iterator;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLFactory;

/**
 * Utility per operazioni su oggetti OpenAPI.
 */
public final class OpenApiUtils {

  private OpenApiUtils() {
  }

  /**
   * Rimuove tutti i campi null (ricorsivamente) da un oggetto OpenAPI.
   *
   * @param openApi l'oggetto OpenAPI da pulire
   * @return una nuova istanza di OpenAPI senza campi null
   */
  public static OpenAPI removeNulls(OpenAPI openApi) {
    if (openApi == null) {
      return null;
    }

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    //mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    // Converti in JsonNode
    JsonNode node = mapper.valueToTree(openApi);

    // Rimuovi i campi null
    removeNullsFromNode(node);

    // 3Riconverti in OpenAPI
    return mapper.treeToValue(node, OpenAPI.class);
  }

  private static void removeNullsFromNode(JsonNode node) {
    if (node instanceof ObjectNode objectNode) {
      Iterator<Map.Entry<String, JsonNode>> fields = objectNode.properties().iterator();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> entry = fields.next();
        JsonNode value = entry.getValue();

        if (value == null || value.isNull()) {
          fields.remove();
        } else {
          removeNullsFromNode(value);
        }
      }
    } else if (node.isArray()) {
      for (JsonNode element : node) {
        removeNullsFromNode(element);
      }
    }
  }
}
