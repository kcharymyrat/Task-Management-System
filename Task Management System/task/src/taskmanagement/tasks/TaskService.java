package taskmanagement.tasks;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import taskmanagement.accounts.AppUser;
import taskmanagement.accounts.AppUserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final AppUserRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(AppUserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Optional<TaskEntity> createTask(String title, String description, UserDetails userDetails) {
        // Get AppUser from UserDetails
        Optional<AppUser> optionalAppUser = userRepository.findByUsernameIgnoreCase(userDetails.getUsername());
        if (optionalAppUser.isEmpty()) {
            return Optional.empty();
        }
        AppUser user = optionalAppUser.get();

        TaskEntity newTask = new TaskEntity();
        newTask.setTitle(title.trim());
        newTask.setDescription(description.trim());

        // Set the author of the task
        newTask.setAuthor(user);

        // Add the new task to the user's tasks
        user.getTasks().add(newTask);

        taskRepository.save(newTask);
        userRepository.save(user);

        return Optional.of(newTask);
    }

    public List<TaskResponseDTO> getAllTasks() {
        List<TaskEntity> taskEntities = (List<TaskEntity>) taskRepository.findAll();
        List<TaskResponseDTO> taskResponseDTOList = taskEntities.stream()
                .map(this::taskEntityToTaskResponseDTO)
                .collect(Collectors.toList());

        Collections.reverse(taskResponseDTOList);

        return taskResponseDTOList;
    }


    public List<TaskResponseDTO> getAllTasks(String author) {
        Optional<AppUser> optionalAppUser = userRepository.findByUsernameIgnoreCase(author.trim().toLowerCase());
        if (optionalAppUser.isEmpty()) {
            return new ArrayList<TaskResponseDTO>();
        }
        AppUser user = optionalAppUser.get();

        List<TaskEntity> taskEntities = (List<TaskEntity>) taskRepository.findAll();

        List<TaskResponseDTO> taskResponseDTOList = taskEntities.stream()
                .filter(task -> task.getAuthor().equals(user))
                .map(this::taskEntityToTaskResponseDTO)
                .collect(Collectors.toList());;

        Collections.reverse(taskResponseDTOList);

        return taskResponseDTOList;
    }

    public TaskResponseDTO taskEntityToTaskResponseDTO(TaskEntity taskEntity) {
        return new TaskResponseDTO(
                taskEntity.getId().toString(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getStatus().toString(),
                taskEntity.getAuthor().getUsername()
        );
    }
}
