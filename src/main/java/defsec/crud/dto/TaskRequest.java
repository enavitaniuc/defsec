package defsec.crud.dto;

import defsec.crud.entity.Task;
import defsec.crud.validation.ValidTaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for Task creation and update requests.
 * Excludes id, createdAt, and updatedAt fields which are managed by the system.
 */
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be under 256 characters")
    private String title;
    
    @Size(max = 500, message = "Description must be under 500 characters")
    private String description;
    
    @ValidTaskStatus
    @Schema(description = "Task status", allowableValues = {"PENDING", "COMPLETED"}, example = "PENDING")
    private String status = Task.Status.PENDING.name();

    // Default constructor
    public TaskRequest() {}

    // Constructor
    public TaskRequest(String title, String description, String status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    // Getters and Setters
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Converts this DTO to a Task entity
     */
    public Task toEntity() {
        Task task = new Task();
        task.setTitle(this.title);
        task.setDescription(this.description);
        task.setStatus(Task.Status.valueOf(this.status));
        return task;
    }

    /**
     * Updates an existing Task entity with values from this DTO
     */
    public void updateEntity(Task task) {
        task.setTitle(this.title);
        task.setDescription(this.description);
        task.setStatus(Task.Status.valueOf(this.status));
    }
} 