package defsec.crud.facade;

import defsec.crud.dto.TaskRequest;
import defsec.crud.entity.Task;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Facade interface for Task operations.
 * Provides a clean abstraction layer for task management operations.
 */
public interface TaskFacade {
    
    /**
     * Retrieves all tasks
     * @return list of all tasks
     */
    List<Task> getAllTasks();
    
    /**
     * Retrieves a task by its ID
     * @param id the task ID
     * @return ResponseEntity with task if found, or 404 if not found
     */
    ResponseEntity<Task> getTaskById(Long id);
    
    /**
     * Creates a new task
     * @param task the task entity to create
     * @return the created task
     * @throws ConflictException if task with same title already exists
     */
    Task createTask(Task task);
    
    /**
     * Updates an existing task
     * @param id the task ID to update
     * @param taskRequest the task data to update
     * @return ResponseEntity with updated task if found, or 404 if not found
     * @throws ConflictException if updated title conflicts with existing task
     */
    ResponseEntity<Task> updateTask(Long id, TaskRequest taskRequest);
    
    /**
     * Deletes a task by its ID
     * @param id the task ID to delete
     * @return ResponseEntity with 200 if deleted, or 404 if not found
     */
    ResponseEntity<Void> deleteTask(Long id);
} 