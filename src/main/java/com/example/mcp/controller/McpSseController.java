package com.example.mcp.controller;

import com.example.mcp.service.JsonRpcService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class McpSseController {

    private final JsonRpcService service;
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public McpSseController(JsonRpcService service) {
        this.service = service;
    }

    @GetMapping("/sse")
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        this.emitters.add(emitter);
        
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        
        try {
            // Send the required 'endpoint' event
            emitter.send(SseEmitter.event().name("endpoint").data("/message"));
            System.err.println("New SSE Client connected.");
        } catch (Exception e) {
            this.emitters.remove(emitter);
        }
        return emitter;
    }

    // Support StreamableHTTP clients that POST to the connection URL
    @PostMapping(value = "/sse")
    public ResponseEntity<String> handleSsePost(@RequestBody String body) {
        return handleMessage(body);
    }

    @PostMapping(value = "/message")
    public ResponseEntity<String> handleMessage(@RequestBody String body) {
        try {
            System.err.println("Received message: " + body);
            String response = service.handleRequest(body);
            
            if (response != null) {
                // Broadcast to SSE clients (legacy support)
                for (SseEmitter emitter : emitters) {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(response));
                    } catch (Exception e) {
                        emitters.remove(emitter);
                    }
                }
                
                // Return direct response for Streamable HTTP clients (Satisfies strict JSON validation)
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
            
            // For notifications (no response needed), return 202 Accepted
            return ResponseEntity.accepted().build();
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\": \"" + e.getMessage() + "\" }");
        }
    }
}