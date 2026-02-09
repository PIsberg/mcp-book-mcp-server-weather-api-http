# Weather Tool MCP Server

This is a Spring Boot application acting as a Model Context Protocol (MCP) server that provides weather information using the WeatherAPI.

## Features

-   **Weather Tool**: Exposes a `weather-tool` that fetches current weather for a specified city.
-   **MCP Support**: Implements the Model Context Protocol over SSE (Server-Sent Events) and HTTP.
-   **Endpoints**:
    -   `GET /sse`: SSE endpoint for MCP connection.
    -   `POST /sse`: Endpoint for Streamable HTTP clients.
    -   `POST /message`: Endpoint for handling JSON-RPC messages.

## Prerequisites

-   Java 21 or later
-   Maven 3.6 or later
-   A [WeatherAPI](https://www.weatherapi.com/) API Key

## Build Instructions

To build the application, run the following command in the project root:

```bash
mvn clean package
```

This will create an executable JAR file in the `target/` directory (e.g., `target/weather-tool-0.0.1-SNAPSHOT.jar`).

## Run Instructions

You can run the application using the Maven plugin:

```bash
mvn spring-boot:run
```

Or by running the built JAR file directly:

```bash
java -jar target/weather-tool-0.0.1-SNAPSHOT.jar
```

The server will start on port **8080** by default.

## Usage

### Tool Input Schema

The `weather-tool` accepts the following parameters:

-   `q` (string, required): City name (e.g., "London").
-   `key` (string, required): Your WeatherAPI key.

### Example JSON-RPC Request

You can test the tool using a POST request to `http://localhost:8080/message`:

```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "params": {
    "name": "weather-tool",
    "arguments": {
      "q": "London",
      "key": "YOUR_API_KEY"
    }
  },
  "id": 1
}
```
