package defsec.crud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "title", nullable = false, unique = true, length = 255)
    private String title;
    
    @Column(name = "description")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.PENDING;
    
    /**
     * Task creation timestamp in UTC.
     * Format: yyyy-MM-dd'T'HH:mm:ss'Z'
     * Example: 2025-01-15T14:30:45Z
     * @format yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    @Schema(description = "Task creation timestamp in UTC", type = "string", format = "yyyy-MM-dd'T'HH:mm:ss'Z'", example = "2025-01-15T14:30:45Z")
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Task last update timestamp in UTC.
     * Format: yyyy-MM-dd'T'HH:mm:ss'Z'
     * Example: 2025-01-15T14:35:20Z
     * @format yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    @Schema(description = "Task update timestamp in UTC", type = "string", format = "yyyy-MM-dd'T'HH:mm:ss'Z'", example = "2025-01-15T14:35:20Z")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * WARNING: This setter is for unit tests ONLY!
     * Do NOT use in production code - timestamps are managed by JPA lifecycle methods.
     * @param createdAt the creation timestamp to set for testing
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        // TODO: add check here to ensure in non test env this should cause Exception
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * WARNING: This setter is for unit tests ONLY!
     * Do NOT use in production code - timestamps are managed by JPA lifecycle methods.
     * @param updatedAt the update timestamp to set for testing
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        // TODO: add check here to ensure in non test env this should cause Exception
        this.updatedAt = updatedAt;
    }

    public enum Status {
        PENDING, COMPLETED;
        
        @com.fasterxml.jackson.annotation.JsonValue
        @Override
        public String toString() {
            return name();
        }
    }
}