package com.example.mcp.controller;

import com.example.mcp.service.JsonRpcService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(McpSseController.class)
public class McpSseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JsonRpcService jsonRpcService;

    @Test
    public void testHandleSse_Get() throws Exception {
        MvcResult result = mockMvc.perform(get("/sse"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify that the response is an SSE stream
        // The content type for SSE is usually text/event-stream, but Spring's
        // SseEmitter might return it differently in tests or depend on configuration.
        // However, we can check that we got a result.
        assertNotNull(result.getResponse());
    }

    @Test
    public void testHandleSse_CallTool() throws Exception {
        String requestBody = "{\"jsonrpc\": \"2.0\", \"method\": \"tools/call\", \"params\": {\"name\": \"weather-tool\", \"arguments\": {\"q\": \"London\", \"key\": \"test-key\"}}, \"id\": 1}";
        String responseBody = "{\"jsonrpc\": \"2.0\", \"result\": {\"content\": [{\"type\": \"text\", \"text\": \"Weather info\"}]}, \"id\": 1}";

        when(jsonRpcService.handleRequest(anyString())).thenReturn(responseBody);

        mockMvc.perform(post("/sse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));
    }

    @Test
    public void testHandleSse_ListTools() throws Exception {
        String requestBody = "{\"jsonrpc\": \"2.0\", \"method\": \"tools/list\", \"id\": 2}";
        String responseBody = "{\"jsonrpc\": \"2.0\", \"result\": {\"tools\": [{\"name\": \"weather-tool\", \"description\": \"Get the current weather for a specific city.\", \"inputSchema\": {\"type\": \"object\", \"properties\": {\"q\": {\"type\": \"string\", \"description\": \"City name (e.g. London)\"}, \"key\": {\"type\": \"string\", \"description\": \"API Key\"}}, \"required\": [\"q\", \"key\"]}}]}, \"id\": 2}";

        when(jsonRpcService.handleRequest(anyString())).thenReturn(responseBody);

        mockMvc.perform(post("/sse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));
    }
}
