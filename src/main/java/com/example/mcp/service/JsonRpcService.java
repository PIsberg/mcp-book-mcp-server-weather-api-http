package com.example.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

/**
 * Example code from Book What you need to know about MCP servers - with Java
 * and spring-boot examples By Peter Isberg
 * URL: TODO
 */

@Service
public class JsonRpcService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ToolHandler toolHandler;

    public JsonRpcService(ToolHandler toolHandler) {
        this.toolHandler = toolHandler;
    }

    public String handleRequest(String line) {
        try {
            if (line == null || line.trim().isEmpty())
                return null;

            JsonNode root = mapper.readTree(line);
            String method = root.has("method") ? root.get("method").asText() : "";
            JsonNode idNode = root.get("id");

            ObjectNode response = mapper.createObjectNode();
            response.put("jsonrpc", "2.0");
            if (idNode != null) {
                response.set("id", idNode);
            } else {
                response.putNull("id");
            }

            if ("initialize".equals(method)) {
                ObjectNode result = response.putObject("result");
                result.put("protocolVersion", "2024-11-05");

                ObjectNode capabilities = result.putObject("capabilities");
                capabilities.putObject("tools");
                // Explicitly declare resources and prompts support (even if empty)
                capabilities.putObject("resources");
                capabilities.putObject("prompts");

                ObjectNode serverInfo = result.putObject("serverInfo");
                serverInfo.put("name", "weather-tool");
                serverInfo.put("version", "1.0");

                return mapper.writeValueAsString(response);
            }

            if ("notifications/initialized".equals(method)) {
                return null;
            }

            if ("tools/list".equals(method)) {
                ObjectNode result = response.putObject("result");
                ArrayNode tools = result.putArray("tools");

                ObjectNode tool = tools.addObject();
                tool.put("name", "weather-tool");
                tool.put("description", "Get the current weather for a specific city.");

                ObjectNode schema = tool.putObject("inputSchema");
                schema.put("type", "object");
                ObjectNode props = schema.putObject("properties");

                props.putObject("q")
                        .put("type", "string")
                        .put("description", "City name (e.g. London)");
                props.putObject("key")
                        .put("type", "string")
                        .put("description", "API Key");

                ArrayNode required = schema.putArray("required");
                required.add("q");
                required.add("key");

                return mapper.writeValueAsString(response);
            }

            // Implement resources/list to prevent timeouts
            if ("resources/list".equals(method)) {
                ObjectNode result = response.putObject("result");
                result.putArray("resources");
                return mapper.writeValueAsString(response);
            }

            // Implement prompts/list to prevent timeouts
            if ("prompts/list".equals(method)) {
                ObjectNode result = response.putObject("result");
                result.putArray("prompts");
                return mapper.writeValueAsString(response);
            }

            if ("tools/call".equals(method)) {
                JsonNode params = root.get("params");
                String toolName = params.has("name") ? params.get("name").asText() : "";

                if (!"weather-tool".equals(toolName)) {
                    throw new RuntimeException("Unknown tool: " + toolName);
                }

                JsonNode args = params.has("arguments") ? params.get("arguments") : mapper.createObjectNode();
                String toolResult = toolHandler.execute(args);

                ObjectNode result = response.putObject("result");
                result.put("isError", false);
                ArrayNode content = result.putArray("content");
                ObjectNode textContent = content.addObject();
                textContent.put("type", "text");
                textContent.put("text", toolResult);

                return mapper.writeValueAsString(response);
            }

            // Handle Unknown Methods to avoid Client Timeouts
            if (idNode != null) {
                ObjectNode err = mapper.createObjectNode();
                err.put("jsonrpc", "2.0");
                err.set("id", idNode);
                ObjectNode errorObj = err.putObject("error");
                errorObj.put("code", -32601);
                errorObj.put("message", "Method not found: " + method);
                return mapper.writeValueAsString(err);
            }

            return null;

        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }

    private String createErrorResponse(String message) {
        try {
            ObjectNode err = mapper.createObjectNode();
            err.put("jsonrpc", "2.0");
            err.putNull("id");
            ObjectNode errorObj = err.putObject("error");
            errorObj.put("code", -32603);
            errorObj.put("message", "Internal Error: " + message);
            return mapper.writeValueAsString(err);
        } catch (Exception x) {
            // Double escaped JSON for fallback
            return "{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -32700, \"message\": \"Parse error\"}, \"id\": null}";
        }
    }
}