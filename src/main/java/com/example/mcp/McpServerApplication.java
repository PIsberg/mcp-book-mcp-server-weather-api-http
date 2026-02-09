package com.example.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import java.io.PrintStream;

@SpringBootApplication
public class McpServerApplication {
    // Capture original stdout before Spring Boot starts
    public static final PrintStream ORIGINAL_OUT = System.out;

    public static void main(String[] args) {
        // Redirect System.out to System.err to prevent logs from polluting the transport
        System.setOut(System.err);
        SpringApplication.run(McpServerApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}