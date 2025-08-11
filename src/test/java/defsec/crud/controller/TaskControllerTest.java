package defsec.crud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import defsec.crud.config.JacksonConfig;
import defsec.crud.dto.TaskRequest;
import defsec.crud.entity.Task;
import defsec.crud.exception.ConflictException;
import defsec.crud.facade.TaskFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(JacksonConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskFacade taskFacade;

    @Autowired
    private ObjectMapper objectMapper;

    private Task testTask;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(Task.Status.PENDING);
        // Set timestamps to simulate a persisted entity
        testTask.setCreatedAt(LocalDateTime.of(2025, 1, 15, 14, 30, 45));
        testTask.setUpdatedAt(LocalDateTime.of(2025, 1, 15, 14, 35, 20));
    }

    @Test
    void getAllTasksShouldReturnListOfTasks() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskFacade.getAllTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].description").value("Test Description"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getTaskByIdWhenTaskExistsShouldReturnTask() throws Exception {
        // Given
        when(taskFacade.getTaskById(1L)).thenReturn(ResponseEntity.ok(testTask));

        // When & Then
        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void getTaskByIdWhenTaskNotExistsShouldReturnNotFound() throws Exception {
        // Given
        when(taskFacade.getTaskById(999L)).thenReturn(ResponseEntity.notFound().build());

        // When & Then
        mockMvc.perform(get("/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTaskWithValidTaskShouldReturnCreatedTask() throws Exception {
        // Given
        TaskRequest newTaskRequest = new TaskRequest();
        newTaskRequest.setTitle("New Task");
        newTaskRequest.setDescription("New Description");
        newTaskRequest.setStatus("PENDING");

        when(taskFacade.createTask(any(Task.class))).thenReturn(testTask);

        // When & Then
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTaskRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void createTaskWithDuplicateTitleShouldReturnConflict() throws Exception {
        // Given
        TaskRequest newTaskRequest = new TaskRequest();
        newTaskRequest.setTitle("Duplicate Task");
        newTaskRequest.setDescription("Description");
        newTaskRequest.setStatus("PENDING");

        when(taskFacade.createTask(any(Task.class)))
                .thenThrow(new ConflictException(
                    "A task with the title 'Duplicate Task' already exists",
                    "title",
                    "Duplicate Task"
                ));

        // When & Then
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTaskRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A task with the title 'Duplicate Task' already exists"))
                .andExpect(jsonPath("$.field").value("title"));
    }

    @Test
    void createTaskWithInvalidTitleShouldReturnBadRequest() throws Exception {
        // Given
        TaskRequest invalidTaskRequest = new TaskRequest();
        invalidTaskRequest.setTitle(""); // Invalid: empty title
        invalidTaskRequest.setDescription("Description");

        // When & Then
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void createTaskWithTitleTooLongShouldReturnBadRequest() throws Exception {
        // Given
        TaskRequest invalidTaskRequest = new TaskRequest();
        invalidTaskRequest.setTitle("a".repeat(256)); // Invalid: title too long
        invalidTaskRequest.setDescription("Description");

        // When & Then
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void createTaskWithInvalidStatusShouldReturnBadRequest() throws Exception {
        // Given
        String invalidTaskJson = """
                {
                    "title": "Valid Title",
                    "description": "Valid Description",
                    "status": "INVALID_STATUS"
                }
                """;

        // When & Then
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTaskJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTaskWithValidTaskShouldReturnUpdatedTask() throws Exception {
        // Given
        TaskRequest updatedTaskRequest = new TaskRequest();
        updatedTaskRequest.setTitle("Updated Task");
        updatedTaskRequest.setDescription("Updated Description");
        updatedTaskRequest.setStatus("COMPLETED");

        testTask.setTitle("Updated Task");
        testTask.setStatus(Task.Status.COMPLETED);

        when(taskFacade.updateTask(eq(1L), any(TaskRequest.class)))
                .thenReturn(ResponseEntity.ok(testTask));

        // When & Then
        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTaskRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }

    @Test
    void updateTaskWhenTaskNotExistsShouldReturnNotFound() throws Exception {
        // Given
        TaskRequest updatedTaskRequest = new TaskRequest();
        updatedTaskRequest.setTitle("Updated Task");
        updatedTaskRequest.setDescription("Updated Description");

        when(taskFacade.updateTask(eq(999L), any(TaskRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        // When & Then
        mockMvc.perform(put("/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTaskRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTaskWithDuplicateTitleShouldReturnConflict() throws Exception {
        // Given
        TaskRequest updatedTaskRequest = new TaskRequest();
        updatedTaskRequest.setTitle("Duplicate Title");
        updatedTaskRequest.setDescription("Description");

        when(taskFacade.updateTask(eq(1L), any(TaskRequest.class)))
                .thenThrow(new ConflictException(
                    "A task with the title 'Duplicate Title' already exists",
                    "title",
                    "Duplicate Title"
                ));

        // When & Then
        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTaskRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A task with the title 'Duplicate Title' already exists"))
                .andExpect(jsonPath("$.field").value("title"));
    }

    @Test
    void deleteTaskWhenTaskExistsShouldReturnOk() throws Exception {
        // Given
        when(taskFacade.deleteTask(1L)).thenReturn(ResponseEntity.ok().build());

        // When & Then
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTaskWhenTaskNotExistsShouldReturnNotFound() throws Exception {
        // Given
        when(taskFacade.deleteTask(999L)).thenReturn(ResponseEntity.notFound().build());

        // When & Then
        mockMvc.perform(delete("/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTaskByIdShouldIncludeTimestampsInResponse() throws Exception {
        // Given
        when(taskFacade.getTaskById(1L)).thenReturn(ResponseEntity.ok(testTask));

        // When & Then
        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.createdAt").value("2025-01-15T14:30:45Z"))
                .andExpect(jsonPath("$.updatedAt").value("2025-01-15T14:35:20Z"));
    }

    @Test
    void getAllTasksShouldIncludeTimestampsInResponse() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskFacade.getAllTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].createdAt").value("2025-01-15T14:30:45Z"))
                .andExpect(jsonPath("$[0].updatedAt").value("2025-01-15T14:35:20Z"));
    }

    @Test
    void createTaskShouldReturnTaskWithTimestamps() throws Exception {
        // Given
        TaskRequest newTaskRequest = new TaskRequest();
        newTaskRequest.setTitle("New Task");
        newTaskRequest.setDescription("New Description");
        newTaskRequest.setStatus("PENDING");

        // Set up the returned task with timestamps
        Task returnedTask = new Task();
        returnedTask.setId(2L);
        returnedTask.setTitle("New Task");
        returnedTask.setDescription("New Description");
        returnedTask.setStatus(Task.Status.PENDING);
        returnedTask.setCreatedAt(LocalDateTime.of(2025, 1, 15, 15, 0, 0));
        // updatedAt should be null for newly created tasks

        when(taskFacade.createTask(any(Task.class))).thenReturn(returnedTask);

        // When & Then
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTaskRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.createdAt").value("2025-01-15T15:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").doesNotExist()); // Should be null/omitted
    }

    @Test
    void updateTaskShouldReturnTaskWithUpdatedTimestamp() throws Exception {
        // Given
        TaskRequest updatedTaskRequest = new TaskRequest();
        updatedTaskRequest.setTitle("Updated Task");
        updatedTaskRequest.setDescription("Updated Description");
        updatedTaskRequest.setStatus("COMPLETED");

        // Set up the returned task with updated timestamp
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated Description");
        updatedTask.setStatus(Task.Status.COMPLETED);
        updatedTask.setCreatedAt(LocalDateTime.of(2025, 1, 15, 14, 30, 45)); // Original creation time
        updatedTask.setUpdatedAt(LocalDateTime.of(2025, 1, 15, 16, 0, 0)); // New update time

        
        when(taskFacade.updateTask(eq(1L), any(TaskRequest.class)))
                .thenReturn(ResponseEntity.ok(updatedTask));

        // When & Then
        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTaskRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.createdAt").value("2025-01-15T14:30:45Z")) // Should remain unchanged
                .andExpect(jsonPath("$.updatedAt").value("2025-01-15T16:00:00Z")); // Should be updated
    }
} 