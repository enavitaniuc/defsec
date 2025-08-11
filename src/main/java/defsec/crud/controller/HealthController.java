package defsec.crud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "defsec-tasks-api");
        
        // Database health check
        Map<String, Object> database = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            // Test database connectivity with a simple query
            boolean isValid = connection.isValid(5); // 5 second timeout
            database.put("status", isValid ? "healthy" : "unhealthy");
            database.put("connection", "ok");
        } catch (Exception e) {
            database.put("status", "unhealthy");
            database.put("connection", "failed");
            database.put("error", e.getMessage());
            
            // If database is down, return 503 Service Unavailable
            response.put("status", "degraded");
        }
        
        response.put("database", database);
        
        // Return 503 if database is unhealthy, 200 if everything is ok
        if ("unhealthy".equals(database.get("status"))) {
            return ResponseEntity.status(503).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}
