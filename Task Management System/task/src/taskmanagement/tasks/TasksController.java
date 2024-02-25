package taskmanagement.tasks;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class TasksController {

    final private TaskService taskService;

    public TasksController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/api/tasks")
    public ResponseEntity<TaskResponseDTO> postTasks(
            @AuthenticationPrincipal UserDetails details,
            @RequestBody @Valid TaskDTO taskDTO
    ) {
        System.out.println("Username: " + details.getUsername());
        System.out.println("User has authorities/roles: " + details.getAuthorities());

        Optional<TaskEntity> optionalTaskEntity = taskService.createTask(taskDTO.title(), taskDTO.description(), details);

        if (optionalTaskEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        TaskEntity taskEntity = optionalTaskEntity.get();
        TaskResponseDTO taskResponseDTO = taskService.taskEntityToTaskResponseDTO(taskEntity);

        return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTO);
    }


    @GetMapping("/api/tasks")
    public ResponseEntity<List<TaskResponseDTO>> getTasks(
            @AuthenticationPrincipal UserDetails details,
            @RequestParam(required = false) String author
    ) {
        System.out.println("Username: " + details.getUsername());
        System.out.println("User has authorities/roles: " + details.getAuthorities());

        if (author == null || author.isBlank() || author.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTasks());
        }
        return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTasks(author));
    }

}
