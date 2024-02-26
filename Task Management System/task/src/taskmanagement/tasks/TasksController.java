package taskmanagement.tasks;

import jakarta.validation.Valid;
import org.aspectj.apache.bcel.classfile.SourceFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import taskmanagement.accounts.AppUser;
import taskmanagement.accounts.AppUserDetailsServiceImpl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
public class TasksController {

    final private TaskService taskService;
    final private AppUserDetailsServiceImpl appUserDetailsService;

    public TasksController(TaskService taskService, AppUserDetailsServiceImpl appUserDetailsService) {
        this.taskService = taskService;
        this.appUserDetailsService = appUserDetailsService;
    }

    @PostMapping("/api/tasks")
    public ResponseEntity<TaskResponseDTO> postTasks(
            @RequestBody @Valid TaskDTO taskDTO
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Username: " + auth.getName());
        System.out.println("User has authorities/roles: " + auth.getAuthorities());

        Optional<TaskEntity> optionalTaskEntity = taskService.createTaskEntity(taskDTO.title(), taskDTO.description(), auth.getName());

        if (optionalTaskEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        TaskEntity taskEntity = optionalTaskEntity.get();
        TaskResponseDTO taskResponseDTO = taskService.taskEntityToTaskResponseDTO(taskEntity);

        return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTO);
    }


    @GetMapping("/api/tasks")
    public ResponseEntity<List<TaskResponseDTO>> getTasks(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String assignee
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Username: " + auth.getName());
        System.out.println("User has authorities/roles: " + auth.getAuthorities());
        System.out.printf("author = %s, assignee = %s\n", author, assignee);

        if (author == null || author.isBlank() || author.isEmpty()) {
            if (assignee == null || assignee.isBlank() || assignee.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTaskResponseDTOs());
            }
            return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllAssigneeTaskResponseDTOs(assignee));
        } else {
            if (assignee == null || assignee.isBlank() || assignee.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTaskResponseDTOs(author));
            }
            return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTaskResponseDTOs(author, assignee));
        }
    }


    @PutMapping("api/tasks/{id}/assign")
    public ResponseEntity<TaskResponseDTO> assignTask(
            @PathVariable Long id,
            @RequestBody AssigneeDTO assigneeDto
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Username: " + auth.getName());
        System.out.println("User has authorities/roles: " + auth.getAuthorities());

        System.out.printf("assigneeDto = %s\n", assigneeDto);

        // Check if task with this id exist
        Optional<TaskEntity> optionalTaskEntity = taskService.getTaskEntityById(id);
        System.out.printf("optionalTaskEntity = %s\n", optionalTaskEntity);
        if (optionalTaskEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        TaskEntity taskEntity = optionalTaskEntity.get();
        System.out.printf("taskEntity = %s\n", taskEntity);

        // Check if author is the same as the authenticated user
        String author = taskEntity.getAuthor().getUsername().trim().toLowerCase();
        String authUsername = auth.getName().trim().toLowerCase();
        System.out.printf("author = %s, authUsername = %s\n", author, authUsername);
        if (!author.equals(authUsername)) {
            System.out.printf("HttpStatus.BAD_REQUEST = %s\n", HttpStatus.FORBIDDEN);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        // check if the assignee valid or if it is "none"
        if (assigneeDto.assignee().trim().equalsIgnoreCase("none")) {
            taskEntity.setAssignee(null);
            TaskEntity updatedTaskEntity = taskService.updateTaskEntity(taskEntity);
            System.out.printf("updatedTaskEntity = %s\n", updatedTaskEntity);

            TaskResponseDTO taskResponseDTO = taskService.taskEntityToTaskResponseDTO(updatedTaskEntity);
            System.out.printf("none case - taskResponseDTO = %s\n", taskResponseDTO);

            return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTO);
        } else {
            Optional<AppUser> optionalAppUser = appUserDetailsService.findByUsernameIgnoreCase(
                    assigneeDto.assignee().trim().toLowerCase()
            );
            System.out.printf("optionalAppUser = %s\n", optionalAppUser);
            if (optionalAppUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            AppUser assigneeAppUser = optionalAppUser.get();
            System.out.printf("assigneeAppUser = %s\n", assigneeAppUser);

            taskEntity.setAssignee(assigneeAppUser);
            TaskEntity updatedTaskEntity = taskService.updateTaskEntity(taskEntity);
            System.out.printf("updatedTaskEntity = %s\n", updatedTaskEntity);

            TaskResponseDTO taskResponseDTO = taskService.taskEntityToTaskResponseDTO(updatedTaskEntity);
            System.out.printf("taskResponseDTO = %s\n", taskResponseDTO);

            return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTO);
        }

    }

    @PutMapping("api/tasks/{id}/status")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody StatusDTO statusDTO
    ) {
        System.out.printf("statusDTO = %s\n", statusDTO);

        // Check if it is valid status
        if (statusDTO == null || statusDTO.status() == null || statusDTO.status().isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String statusUpperCase = statusDTO.status().trim().toUpperCase();
        TaskStatus taskStatus;
        if (statusUpperCase.equalsIgnoreCase("CREATED")) {
            taskStatus = TaskStatus.CREATED;
        } else if (statusUpperCase.equalsIgnoreCase("IN_PROGRESS")) {
            taskStatus = TaskStatus.IN_PROGRESS;
        } else if(statusUpperCase.equalsIgnoreCase("COMPLETED")) {
            taskStatus = TaskStatus.COMPLETED;
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Username: " + auth.getName());
        System.out.println("User has authorities/roles: " + auth.getAuthorities());

        System.out.printf("statusDTO = %s\n", statusDTO);

        // Check if task with this id exist
        Optional<TaskEntity> optionalTaskEntity = taskService.getTaskEntityById(id);
        System.out.printf("optionalTaskEntity = %s\n", optionalTaskEntity);
        if (optionalTaskEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        TaskEntity taskEntity = optionalTaskEntity.get();
        System.out.printf("taskEntity = %s\n", taskEntity);

        // Check if author is the same as the authenticated user or as assignee
        String author = taskEntity.getAuthor().getUsername().trim().toLowerCase();
        String authUsername = auth.getName().trim().toLowerCase();
        System.out.printf("author = %s, authUsername = %s\n", author, authUsername);
        if (!author.equalsIgnoreCase(authUsername)) {
            if (taskEntity.getAssignee() == null) {
                System.out.printf("HttpStatus.BAD_REQUEST = %s\n", HttpStatus.FORBIDDEN);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            System.out.printf("taskEntity.getAssignee() = %s, authUsername = %s\n", taskEntity.getAssignee(), authUsername);
            String assignee = taskEntity.getAssignee().getUsername().trim().toLowerCase();
            if (!author.equalsIgnoreCase(authUsername) && !assignee.equalsIgnoreCase(authUsername)) {
                System.out.printf("HttpStatus.BAD_REQUEST = %s\n", HttpStatus.FORBIDDEN);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        taskEntity.setStatus(taskStatus);

        TaskEntity updatedTaskEntity = taskService.updateTaskEntity(taskEntity);
        System.out.printf("updatedTaskEntity = %s\n", updatedTaskEntity);

        TaskResponseDTO taskResponseDTO = taskService.taskEntityToTaskResponseDTO(updatedTaskEntity);
        System.out.printf("none case - taskResponseDTO = %s\n", taskResponseDTO);

        return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTO);
    }
}
