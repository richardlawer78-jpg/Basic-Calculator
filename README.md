# Health Check Service

A Spring Boot application that monitors the health status of multiple endpoints and provides REST API endpoints to check their status.

## Features

- **Real-time Health Monitoring**: Continuously monitors endpoint health status
- **REST API Endpoints**: Provides multiple endpoints to check health status
- **Asynchronous Processing**: Non-blocking health checks for better performance
- **Background Monitoring**: Runs health checks in the background every 30 seconds
- **Detailed Status Information**: Returns comprehensive health status information

## API Endpoints

### 1. Get All Health Status

```
GET /health-check/api/health
```

Returns the health status of all endpoints.

**Response:**

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "ENDPOINT_ONE": true,
  "ENDPOINT_TWO": false,
  "ENDPOINT_THREE": true,
  "overall_status": "SOME_UNHEALTHY"
}
```

### 2. Get Individual Endpoint Health

```
GET /health-check/api/health/{endpoint}
```

Check the health of a specific endpoint (1, 2, or 3).

**Example:**

```
GET /health-check/api/health/1
```

### 3. Get Health Summary

```
GET /health-check/api/health/summary
```

Returns a quick summary of all endpoint statuses.

## Running the Application

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Build and Run

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/health-check-service-1.0.0.jar
```

### Using Maven

```bash
# Run directly with Maven
mvn spring-boot:run
```

## Configuration

The application can be configured using `application.properties`:

- `server.port`: Server port (default: 8080)
- `health.check.interval`: Health check interval in milliseconds (default: 30000)
- `health.check.timeout`: Request timeout in milliseconds (default: 10000)

## Monitored Endpoints

The service monitors these endpoints:

- `https://prod.npontu.io/endpoint_1/health_check`
- `https://prod.npontu.io/endpoint_2/health_check`
- `https://prod.npontu.io/endpoint_3/health_check`

## Health Check Logic

- **Healthy**: HTTP status codes 200-299
- **Unhealthy**: Any other status code or connection failure
- **Timeout**: 10 seconds for each request
- **Monitoring Interval**: Every 30 seconds

## Example Usage

### Check all endpoints:

```bash
curl http://localhost:8080/health-check/api/health
```

### Check specific endpoint:

```bash
curl http://localhost:8080/health-check/api/health/1
```

### Get summary:

```bash
curl http://localhost:8080/health-check/api/health/summary
```

## Error Handling

The service handles various error scenarios:

- Network timeouts
- Connection failures
- Invalid endpoint responses
- Server errors

All errors are logged and returned in the API response with appropriate HTTP status codes.
