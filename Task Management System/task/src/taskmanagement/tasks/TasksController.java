package taskmanagement.tasks;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import taskmanagement.accounts.AppUserDetailsImpl;

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
            @RequestBody @Valid TaskDTO taskDTO
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Username: " + auth.getName());
        System.out.println("User has authorities/roles: " + auth.getAuthorities());

        Optional<TaskEntity> optionalTaskEntity = taskService.createTask(taskDTO.title(), taskDTO.description(), auth.getName());

        if (optionalTaskEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        TaskEntity taskEntity = optionalTaskEntity.get();
        TaskResponseDTO taskResponseDTO = taskService.taskEntityToTaskResponseDTO(taskEntity);

        return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTO);
    }


    @GetMapping("/api/tasks")
    public ResponseEntity<List<TaskResponseDTO>> getTasks(
            @RequestParam(required = false) String author
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Username: " + auth.getName());
        System.out.println("User has authorities/roles: " + auth.getAuthorities());

        if (author == null || author.isBlank() || author.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTasks());
        }
        return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTasks(author));
    }

}
