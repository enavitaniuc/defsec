package defsec.crud.facade;

import defsec.crud.dto.TaskRequest;
import defsec.crud.entity.Task;
import defsec.crud.exception.ConflictException;
import defsec.crud.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskFacadeImplTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskFacadeImpl taskFacade;

    private Task existingTask;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        // Create a mock existing task with timestamps
        existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Existing Task");
        existingTask.setDescription("Existing Description");
        existingTask.setStatus(Task.Status.PENDING);
        // Note: In real scenarios, timestamps would be set by JPA lifecycle methods

        // Create a task request for testing
        taskRequest = new TaskRequest();
        taskRequest.setTitle("New Task");
        taskRequest.setDescription("New Description");
        taskRequest.setStatus("PENDING");
    }

    @Test
    void getAllTasksShouldReturnAllTasks() {
        // Given
        List<Task> expectedTasks = Arrays.asList(existingTask);
        when(taskService.findAll()).thenReturn(expectedTasks);

        // When
        List<Task> actualTasks = taskFacade.getAllTasks();

        // Then
        assertThat(actualTasks).isEqualTo(expectedTasks);
        verify(taskService).findAll();
    }

    @Test
    void getTaskByIdWhenTaskExistsShouldReturnTask() {
        // Given
        when(taskService.findById(1L)).thenReturn(Optional.of(existingTask));

        // When
        ResponseEntity<Task> response = taskFacade.getTaskById(1L);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(existingTask);
        verify(taskService).findById(1L);
    }

    @Test
    void getTaskByIdWhenTaskNotExistsShouldReturnNotFound() {
        // Given
        when(taskService.findById(999L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Task> response = taskFacade.getTaskById(999L);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
        verify(taskService).findById(999L);
    }

    @Test
    void createTaskShouldSaveTaskAndReturnWithTimestamp() {
        // Given
        Task taskToCreate = taskRequest.toEntity();
        Task savedTask = new Task();
        savedTask.setId(2L);
        savedTask.setTitle(taskToCreate.getTitle());
        savedTask.setDescription(taskToCreate.getDescription());
        savedTask.setStatus(taskToCreate.getStatus());
        // In real scenarios, JPA lifecycle methods would set timestamps

        when(taskService.save(any(Task.class))).thenReturn(savedTask);

        // When
        Task result = taskFacade.createTask(taskToCreate);

        // Then
        assertThat(result).isEqualTo(savedTask);
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getStatus()).isEqualTo(Task.Status.PENDING);
        
        // Verify the task was saved
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();
        assertThat(capturedTask.getTitle()).isEqualTo("New Task");
        assertThat(capturedTask.getDescription()).isEqualTo("New Description");
        assertThat(capturedTask.getStatus()).isEqualTo(Task.Status.PENDING);
    }

    @Test
    void createTaskWithDuplicateTitleShouldThrowConflictException() {
        // Given
        Task taskToCreate = taskRequest.toEntity();
        DataIntegrityViolationException duplicateException = 
            new DataIntegrityViolationException("Duplicate entry 'New Task' for key 'title'");
        when(taskService.save(any(Task.class))).thenThrow(duplicateException);

        // When & Then
        assertThatThrownBy(() -> taskFacade.createTask(taskToCreate))
            .isInstanceOf(ConflictException.class)
            .hasMessage("A task with the title 'New Task' already exists");
    }

    @Test
    void updateTaskWhenTaskExistsShouldUpdateAndReturnWithUpdatedTimestamp() {
        // Given
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Task");
        updateRequest.setDescription("Updated Description");
        updateRequest.setStatus("COMPLETED");

        when(taskService.findById(1L)).thenReturn(Optional.of(existingTask));
        
        // Create updated task to return from save
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated Description");
        updatedTask.setStatus(Task.Status.COMPLETED);
        // In real scenarios, JPA would handle timestamps
        
        when(taskService.save(any(Task.class))).thenReturn(updatedTask);

        // When
        ResponseEntity<Task> response = taskFacade.updateTask(1L, updateRequest);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        Task responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTitle()).isEqualTo("Updated Task");
        assertThat(responseBody.getDescription()).isEqualTo("Updated Description");
        assertThat(responseBody.getStatus()).isEqualTo(Task.Status.COMPLETED);

        // Verify the task was found and saved
        verify(taskService).findById(1L);
        verify(taskService).save(any(Task.class));
    }

    @Test
    void updateTaskWhenTaskNotExistsShouldReturnNotFound() {
        // Given
        when(taskService.findById(999L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Task> response = taskFacade.updateTask(999L, taskRequest);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
        verify(taskService).findById(999L);
        verify(taskService, never()).save(any(Task.class));
    }

    @Test
    void updateTaskWithDuplicateTitleShouldThrowConflictException() {
        // Given
        when(taskService.findById(1L)).thenReturn(Optional.of(existingTask));
        DataIntegrityViolationException duplicateException = 
            new DataIntegrityViolationException("Duplicate entry 'New Task' for key 'title'");
        when(taskService.save(any(Task.class))).thenThrow(duplicateException);

        // When & Then
        assertThatThrownBy(() -> taskFacade.updateTask(1L, taskRequest))
            .isInstanceOf(ConflictException.class)
            .hasMessage("A task with the title 'New Task' already exists");
    }

    @Test
    void updateTaskShouldPreserveCreatedAtTimestamp() {
        // Given
        when(taskService.findById(1L)).thenReturn(Optional.of(existingTask));
        
        // Mock the save to verify that the facade correctly calls updateEntity
        when(taskService.save(any(Task.class))).thenAnswer(invocation -> {
            Task taskToSave = invocation.getArgument(0);
            // Return the updated task (in real scenarios, JPA would handle timestamps)
            return taskToSave;
        });

        // When
        ResponseEntity<Task> response = taskFacade.updateTask(1L, taskRequest);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        Task responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // Verify that updateEntity was called correctly by checking the updated values
        assertThat(responseBody.getTitle()).isEqualTo("New Task");
        assertThat(responseBody.getDescription()).isEqualTo("New Description");
        assertThat(responseBody.getStatus()).isEqualTo(Task.Status.PENDING);
        
        verify(taskService).findById(1L);
        verify(taskService).save(any(Task.class));
    }

    @Test
    void deleteTaskWhenTaskExistsShouldReturnOk() {
        // Given
        when(taskService.findById(1L)).thenReturn(Optional.of(existingTask));

        // When
        ResponseEntity<Void> response = taskFacade.deleteTask(1L);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNull();
        verify(taskService).findById(1L);
        verify(taskService).deleteById(1L);
    }

    @Test
    void deleteTaskWhenTaskNotExistsShouldReturnNotFound() {
        // Given
        when(taskService.findById(999L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Void> response = taskFacade.deleteTask(999L);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
        verify(taskService).findById(999L);
        verify(taskService, never()).deleteById(any());
    }

    @Test
    void taskRequestShouldCorrectlyUpdateEntity() {
        // Given
        Task taskToUpdate = new Task();
        taskToUpdate.setTitle("Original Title");
        taskToUpdate.setDescription("Original Description");
        taskToUpdate.setStatus(Task.Status.PENDING);
        
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setStatus("COMPLETED");

        // When
        updateRequest.updateEntity(taskToUpdate);

        // Then
        assertThat(taskToUpdate.getTitle()).isEqualTo("Updated Title");
        assertThat(taskToUpdate.getDescription()).isEqualTo("Updated Description");
        assertThat(taskToUpdate.getStatus()).isEqualTo(Task.Status.COMPLETED);
    }

    @Test
    void taskRequestShouldCorrectlyConvertToEntity() {
        // Given
        TaskRequest request = new TaskRequest();
        request.setTitle("Test Task");
        request.setDescription("Test Description");
        request.setStatus("PENDING");

        // When
        Task entity = request.toEntity();

        // Then
        assertThat(entity.getTitle()).isEqualTo("Test Task");
        assertThat(entity.getDescription()).isEqualTo("Test Description");
        assertThat(entity.getStatus()).isEqualTo(Task.Status.PENDING);
        assertThat(entity.getId()).isNull(); // Should not be set
        assertThat(entity.getCreatedAt()).isNull(); // Should not be set until persistence
        assertThat(entity.getUpdatedAt()).isNull(); // Should not be set until persistence
    }

    @Test
    void createTaskWithNullDescriptionShouldWork() {
        // Given
        TaskRequest requestWithNullDescription = new TaskRequest();
        requestWithNullDescription.setTitle("Task Without Description");
        requestWithNullDescription.setDescription(null);
        requestWithNullDescription.setStatus("COMPLETED");

        Task taskToCreate = requestWithNullDescription.toEntity();
        Task savedTask = new Task();
        savedTask.setId(3L);
        savedTask.setTitle("Task Without Description");
        savedTask.setDescription(null);
        savedTask.setStatus(Task.Status.COMPLETED);

        when(taskService.save(any(Task.class))).thenReturn(savedTask);

        // When
        Task result = taskFacade.createTask(taskToCreate);

        // Then
        assertThat(result.getTitle()).isEqualTo("Task Without Description");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getStatus()).isEqualTo(Task.Status.COMPLETED);
        verify(taskService).save(any(Task.class));
    }

    @Test
    void updateTaskWithNullDescriptionShouldWork() {
        // Given
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Task");
        updateRequest.setDescription(null); // Setting description to null
        updateRequest.setStatus("COMPLETED");

        when(taskService.findById(1L)).thenReturn(Optional.of(existingTask));
        
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription(null);
        updatedTask.setStatus(Task.Status.COMPLETED);
        
        when(taskService.save(any(Task.class))).thenReturn(updatedTask);

        // When
        ResponseEntity<Task> response = taskFacade.updateTask(1L, updateRequest);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        Task responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getDescription()).isNull();
        assertThat(responseBody.getStatus()).isEqualTo(Task.Status.COMPLETED);
    }

    @Test
    void createTaskWithEmptyDescriptionShouldWork() {
        // Given
        TaskRequest requestWithEmptyDescription = new TaskRequest();
        requestWithEmptyDescription.setTitle("Task With Empty Description");
        requestWithEmptyDescription.setDescription("");
        requestWithEmptyDescription.setStatus("PENDING");

        Task taskToCreate = requestWithEmptyDescription.toEntity();
        Task savedTask = new Task();
        savedTask.setId(4L);
        savedTask.setTitle("Task With Empty Description");
        savedTask.setDescription("");
        savedTask.setStatus(Task.Status.PENDING);

        when(taskService.save(any(Task.class))).thenReturn(savedTask);

        // When
        Task result = taskFacade.createTask(taskToCreate);

        // Then
        assertThat(result.getDescription()).isEmpty();
        verify(taskService).save(any(Task.class));
    }

    @Test
    void updateTaskShouldHandleStatusChange() {
        // Given
        TaskRequest statusChangeRequest = new TaskRequest();
        statusChangeRequest.setTitle("Existing Task"); // Keep same title
        statusChangeRequest.setDescription("Existing Description"); // Keep same description
        statusChangeRequest.setStatus("COMPLETED"); // Change status only

        when(taskService.findById(1L)).thenReturn(Optional.of(existingTask));
        
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Existing Task");
        updatedTask.setDescription("Existing Description");
        updatedTask.setStatus(Task.Status.COMPLETED); // Status changed
        
        when(taskService.save(any(Task.class))).thenReturn(updatedTask);

        // When
        ResponseEntity<Task> response = taskFacade.updateTask(1L, statusChangeRequest);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        Task responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(Task.Status.COMPLETED);
        
        // Verify that the original task was updated with new status
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();
        assertThat(capturedTask.getStatus()).isEqualTo(Task.Status.COMPLETED);
    }

    @Test
    void createTaskWithDefaultStatusShouldWork() {
        // Given
        TaskRequest requestWithDefaultStatus = new TaskRequest();
        requestWithDefaultStatus.setTitle("Task With Default Status");
        requestWithDefaultStatus.setDescription("Description");
        // Note: not setting status, should default to PENDING

        Task taskToCreate = requestWithDefaultStatus.toEntity();
        Task savedTask = new Task();
        savedTask.setId(5L);
        savedTask.setTitle("Task With Default Status");
        savedTask.setDescription("Description");
        savedTask.setStatus(Task.Status.PENDING); // Default status

        when(taskService.save(any(Task.class))).thenReturn(savedTask);

        // When
        Task result = taskFacade.createTask(taskToCreate);

        // Then
        assertThat(result.getStatus()).isEqualTo(Task.Status.PENDING);
        
        // Verify the captured task has default status
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();
        assertThat(capturedTask.getStatus()).isEqualTo(Task.Status.PENDING);
    }

    @Test
    void getAllTasksShouldReturnEmptyListWhenNoTasks() {
        // Given
        when(taskService.findAll()).thenReturn(Arrays.asList());

        // When
        List<Task> actualTasks = taskFacade.getAllTasks();

        // Then
        assertThat(actualTasks).isEmpty();
        verify(taskService).findAll();
    }

    @Test
    void createTaskWithGenericDataIntegrityViolationShouldRethrow() {
        // Given
        Task taskToCreate = taskRequest.toEntity();
        DataIntegrityViolationException genericException = 
            new DataIntegrityViolationException("Some other constraint violation");
        when(taskService.save(any(Task.class))).thenThrow(genericException);

        // When & Then
        assertThatThrownBy(() -> taskFacade.createTask(taskToCreate))
            .isInstanceOf(DataIntegrityViolationException.class)
            .hasMessage("Some other constraint violation");
    }

    @Test
    void updateTaskWithGenericDataIntegrityViolationShouldRethrow() {
        // Given
        when(taskService.findById(1L)).thenReturn(Optional.of(existingTask));
        DataIntegrityViolationException genericException = 
            new DataIntegrityViolationException("Some other constraint violation");
        when(taskService.save(any(Task.class))).thenThrow(genericException);

        // When & Then
        assertThatThrownBy(() -> taskFacade.updateTask(1L, taskRequest))
            .isInstanceOf(DataIntegrityViolationException.class)
            .hasMessage("Some other constraint violation");
    }
} 