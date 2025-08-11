package defsec.crud.controller;

import defsec.crud.dto.TaskRequest;
import defsec.crud.entity.Task;
import defsec.crud.facade.TaskFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@Validated
public class TaskController {

    @Autowired
    private TaskFacade taskFacade;

    @GetMapping
    public List<Task> getAllTasks() {
        return taskFacade.getAllTasks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskFacade.getTaskById(id);
    }

    @PostMapping
    public Task createTask(@Valid @RequestBody TaskRequest taskRequest) {
        Task task = taskRequest.toEntity();
        return taskFacade.createTask(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest taskRequest) {
        return taskFacade.updateTask(id, taskRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        return taskFacade.deleteTask(id);
    }
} 