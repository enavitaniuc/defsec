package defsec.crud.facade;

import defsec.crud.dto.TaskRequest;
import defsec.crud.entity.Task;
import defsec.crud.exception.ConflictException;
import defsec.crud.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of TaskFacade interface.
 * Contains business logic for task operations.
 */
@Component
public class TaskFacadeImpl implements TaskFacade {

    private static final Logger logger = LoggerFactory.getLogger(TaskFacadeImpl.class);

    @Autowired
    private TaskService taskService;

    @Override
    public List<Task> getAllTasks() {
        logger.debug("Fetching all tasks");
        List<Task> tasks = taskService.findAll();
        logger.info("Retrieved {} tasks", tasks.size());
        return tasks;
    }

    @Override
    public ResponseEntity<Task> getTaskById(Long id) {
        logger.debug("Fetching task with id: {}", id);
        Optional<Task> task = taskService.findById(id);
        
        if (task.isPresent()) {
            logger.debug("Task found with id: {}", id);
            return ResponseEntity.ok(task.get());
        } else {
            logger.warn("Task not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public Task createTask(Task task) {
        logger.info("Creating new task with title: '{}'", task.getTitle());
        
        // Add task title to MDC for structured logging
        MDC.put("taskTitle", task.getTitle());
        
        try {
            Task savedTask = taskService.save(task);
            logger.info("Successfully created task with id: {} and title: '{}'", 
                       savedTask.getId(), savedTask.getTitle());
            return savedTask;
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                logger.warn("Attempted to create task with duplicate title: '{}'", task.getTitle());
                throw new ConflictException(
                    "A task with the title '" + task.getTitle() + "' already exists",
                    "title",
                    task.getTitle()
                );
            }
            logger.error("Database error while creating task with title: '{}'", task.getTitle(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Override
    public ResponseEntity<Task> updateTask(Long id, TaskRequest taskRequest) {
        logger.info("Updating task with id: {} and title: '{}'", id, taskRequest.getTitle());
        
        // Add context to MDC
        MDC.put("taskId", id.toString());
        MDC.put("taskTitle", taskRequest.getTitle());
        
        try {
            Optional<Task> existingTask = taskService.findById(id);
            if (existingTask.isEmpty()) {
                logger.warn("Attempted to update non-existent task with id: {}", id);
                return ResponseEntity.notFound().build();
            }

            Task updatedTask = existingTask.get();
            String originalTitle = updatedTask.getTitle();
            taskRequest.updateEntity(updatedTask);

            try {
                Task savedTask = taskService.save(updatedTask);
                logger.info("Successfully updated task id: {} from title '{}' to '{}'", 
                           id, originalTitle, savedTask.getTitle());
                return ResponseEntity.ok(savedTask);
            } catch (DataIntegrityViolationException e) {
                if (e.getMessage().contains("Duplicate entry")) {
                    logger.warn("Attempted to update task id: {} with duplicate title: '{}'", 
                               id, taskRequest.getTitle());
                    throw new ConflictException(
                        "A task with the title '" + taskRequest.getTitle() + "' already exists",
                        "title",
                        taskRequest.getTitle()
                    );
                }
                logger.error("Database error while updating task id: {}", id, e);
                throw e;
            }
        } finally {
            MDC.clear();
        }
    }

    @Override
    public ResponseEntity<Void> deleteTask(Long id) {
        logger.info("Deleting task with id: {}", id);
        
        MDC.put("taskId", id.toString());
        
        try {
            if (taskService.findById(id).isEmpty()) {
                logger.warn("Attempted to delete non-existent task with id: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            taskService.deleteById(id);
            logger.info("Successfully deleted task with id: {}", id);
            return ResponseEntity.ok().build();
        } finally {
            MDC.clear();
        }
    }
} 