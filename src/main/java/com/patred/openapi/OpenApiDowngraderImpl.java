package com.patred.openapi;

import com.patred.openapi.model.Format;
import com.patred.openapi.util.FormatUtils;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLFactory;

/**
 * Converter da OpenAPI 3.x → Swagger 2.0. Compatibile con tools.jackson 3.x (nessun uso di
 * fieldNames()/fields()).
 */
public class OpenApiDowngraderImpl implements OpenApiDowngrader {

  private final ObjectMapper jsonMapper = new ObjectMapper();
  private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

  public String convertToV2(String spec) throws Exception {
    Format format = FormatUtils.isYaml(spec) ? Format.YAML : Format.JSON;
    return convertToV2(spec, format);
  }

  public String convertToV2(String spec, Format format) throws Exception {
    ObjectMapper inputMapper = FormatUtils.isYaml(spec) ? yamlMapper : jsonMapper;
    ObjectMapper outputMapper = format == Format.YAML ? yamlMapper : jsonMapper;

    JsonNode openApiNode = inputMapper.readTree(spec);
    if (!openApiNode.has("openapi")) {
      throw new IllegalArgumentException("Specifica non valida: manca il campo 'openapi'");
    }

    ObjectNode swagger = outputMapper.createObjectNode();
    swagger.put("swagger", "2.0");

    // info
    if (openApiNode.has("info")) {
      swagger.set("info", openApiNode.get("info"));
    }

    // servers → host/basePath/schemes
    if (openApiNode.has("servers")) {
      JsonNode servers = openApiNode.get("servers");
      if (servers.isArray() && servers.size() > 0) {
        JsonNode server = servers.get(0);
        if (server.has("url")) {
          parseServerUrl(swagger, server.get("url").asText());
        }
      }
    }

    // paths
    if (openApiNode.has("paths")) {
      swagger.set("paths", convertPaths(openApiNode.get("paths"), outputMapper));
    }

    // components.schemas → definitions
    if (openApiNode.path("components").has("schemas")) {
      swagger.set("definitions", openApiNode.path("components").path("schemas"));
    }

    // components.securitySchemes → securityDefinitions
    if (openApiNode.path("components").has("securitySchemes")) {
      swagger.set("securityDefinitions", openApiNode.path("components").path("securitySchemes"));
    }

    return outputMapper.writerWithDefaultPrettyPrinter().writeValueAsString(swagger);
  }


  private ObjectNode convertPaths(JsonNode pathsNode, ObjectMapper mapper) {
    ObjectNode paths = mapper.createObjectNode();
    if (!(pathsNode instanceof ObjectNode pathObj)) {
      return paths;
    }

    Iterator<Map.Entry<String, JsonNode>> it = pathObj.properties().iterator();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> entry = it.next();
      String path = entry.getKey();
      JsonNode pathItem = entry.getValue();

      ObjectNode newPath = mapper.createObjectNode();
      for (String method : new String[]{"get", "post", "put", "delete", "patch", "options",
          "head"}) {
        if (pathItem.has(method)) {
          newPath.set(method, convertOperation(pathItem.get(method), mapper));
        }
      }
      paths.set(path, newPath);
    }
    return paths;
  }

  private ObjectNode convertOperation(JsonNode operation, ObjectMapper mapper) {
    ObjectNode newOp = mapper.createObjectNode();

    copyIfPresent(operation, newOp, "summary");
    copyIfPresent(operation, newOp, "description");
    copyIfPresent(operation, newOp, "operationId");
    copyIfPresent(operation, newOp, "tags");

    ArrayNode parameters = mapper.createArrayNode();

    // parameters (mantieni eventuali già esistenti)
    if (operation.has("parameters") && operation.get("parameters").isArray()) {
      for (JsonNode p : operation.get("parameters")) {
        parameters.add(p);
      }
    }

    // requestBody → body param
    if (operation.has("requestBody")) {
      JsonNode requestBody = operation.get("requestBody");
      ObjectNode bodyParam = mapper.createObjectNode();
      bodyParam.put("in", "body");
      bodyParam.put("name", "body");
      if (requestBody.has("description")) {
        bodyParam.put("description", requestBody.get("description").asText());
      }

      JsonNode content = requestBody.path("content");
      if (content instanceof ObjectNode cObj && !cObj.isEmpty()) {
        Map.Entry<String, JsonNode> firstEntry = cObj.properties().iterator().next();
        JsonNode mediaTypeNode = firstEntry.getValue();
        if (mediaTypeNode.has("schema")) {
          bodyParam.set("schema", mediaTypeNode.get("schema"));
        }
      }
      parameters.add(bodyParam);
    }

    if (parameters.size() > 0) {
      newOp.set("parameters", parameters);
    }

    // responses
    if (operation.has("responses") && operation.get("responses") instanceof ObjectNode respObj) {
      ObjectNode responses = mapper.createObjectNode();
      Iterator<Map.Entry<String, JsonNode>> rfields = respObj.properties().iterator();

      while (rfields.hasNext()) {
        Map.Entry<String, JsonNode> resp = rfields.next();
        ObjectNode newResp = mapper.createObjectNode();
        copyIfPresent(resp.getValue(), newResp, "description");

        JsonNode content = resp.getValue().path("content");
        if (content instanceof ObjectNode cObj && !cObj.isEmpty()) {
          Map.Entry<String, JsonNode> firstEntry = cObj.properties().iterator().next();
          JsonNode media = firstEntry.getValue();
          if (media.has("schema")) {
            newResp.set("schema", media.get("schema"));
          }
        }

        responses.set(resp.getKey(), newResp);
      }
      newOp.set("responses", responses);
    }

    return newOp;
  }

  private void copyIfPresent(JsonNode from, ObjectNode to, String field) {
    if (from.has(field)) {
      to.set(field, from.get(field));
    }
  }

  private void parseServerUrl(ObjectNode swagger, String url) {
    try {
      URI uri = URI.create(url);
      if (uri.getHost() != null) {
        swagger.put("host", uri.getHost());
      }
      if (uri.getPath() != null && !uri.getPath().isEmpty()) {
        swagger.put("basePath", uri.getPath());
      }
      if (uri.getScheme() != null) {
        ArrayNode schemes = swagger.putArray("schemes");
        schemes.add(uri.getScheme());
      }
    } catch (Exception ignored) {
    }
  }
}
