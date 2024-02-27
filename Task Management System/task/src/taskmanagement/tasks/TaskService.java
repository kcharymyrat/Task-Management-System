package taskmanagement.tasks;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import taskmanagement.accounts.AppUser;
import taskmanagement.accounts.AppUserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final AppUserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CommentsRepository commentsRepository;

    @Autowired
    public TaskService(AppUserRepository userRepository, TaskRepository taskRepository, CommentsRepository commentsRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.commentsRepository = commentsRepository;
    }

    public CommentEntity createCommentEntity(String commentText, TaskEntity taskEntity, AppUser appUser) {
        String text = commentText.trim();
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setComment(text);
        commentEntity.setTask(taskEntity);
        commentEntity.setCommenter(appUser);
        commentsRepository.save(commentEntity);

        // Add CommentEntity for both TaskEntity and CommentEntity
        taskEntity.addComment(commentEntity);
        appUser.addComment(commentEntity);
        taskRepository.save(taskEntity);
        userRepository.save(appUser);

        return commentEntity;
    }




    @Transactional
    public Optional<TaskEntity> createTaskEntity(String title, String description, String username) {
        // Get AppUser from UserDetails
        Optional<AppUser> optionalAppUser = userRepository.findByUsernameIgnoreCase(username);
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

    public Optional<TaskEntity> getTaskEntityById(Long taskId) {
        return taskRepository.findById(taskId);
    }

    public TaskEntity updateTaskEntity(TaskEntity taskEntity) {
        taskRepository.save(taskEntity);
        return taskEntity;
    }

    public List<TaskResponseGetDTO> getAllTaskResponseDTOs() {
        List<TaskEntity> taskEntities = (List<TaskEntity>) taskRepository.findAll();
        List<TaskResponseGetDTO> taskResponseDTOList = taskEntities.stream()
                .map(this::taskEntityToTaskResponseGetDTO)
                .collect(Collectors.toList());

        Collections.reverse(taskResponseDTOList);

        return taskResponseDTOList;
    }

    public List<TaskResponseGetDTO> getAllTaskResponseDTOs(String author, String assignee) {
        Optional<AppUser> optionalAuthorAppUser = userRepository.findByUsernameIgnoreCase(author.trim().toLowerCase());
        Optional<AppUser> optionalAssigneeAppUser = userRepository.findByUsernameIgnoreCase(assignee.trim().toLowerCase());

        if (optionalAuthorAppUser.isEmpty() || optionalAssigneeAppUser.isEmpty()) {
            return new ArrayList<TaskResponseGetDTO>();
        }

        AppUser authorUser = optionalAuthorAppUser.get();
        AppUser assigneeUser = optionalAssigneeAppUser.get();

        List<TaskEntity> taskEntities = (List<TaskEntity>) taskRepository.findAll();

        List<TaskResponseGetDTO> taskResponseDTOList = taskEntities.stream()
                .filter(task -> task.getAuthor().equals(authorUser))
                .filter(task -> task.getAssignee() != null && task.getAssignee().equals(assigneeUser))
                .map(this::taskEntityToTaskResponseGetDTO)
                .collect(Collectors.toList());;

        Collections.reverse(taskResponseDTOList);

        return taskResponseDTOList;
    }

    public List<TaskResponseGetDTO> getAllAssigneeTaskResponseDTOs(String assignee) {
        Optional<AppUser> optionalAppUser = userRepository.findByUsernameIgnoreCase(assignee.trim().toLowerCase());
        if (optionalAppUser.isEmpty()) {
            return new ArrayList<TaskResponseGetDTO>();
        }
        AppUser user = optionalAppUser.get();

        List<TaskEntity> taskEntities = (List<TaskEntity>) taskRepository.findAll();

        List<TaskResponseGetDTO> taskResponseDTOList = taskEntities.stream()
                .filter(task -> task.getAssignee() != null && task.getAssignee().equals(user))
                .map(this::taskEntityToTaskResponseGetDTO)
                .collect(Collectors.toList());;

        Collections.reverse(taskResponseDTOList);

        return taskResponseDTOList;
    }


    public List<TaskResponseGetDTO> getAllTaskResponseDTOs(String author) {
        Optional<AppUser> optionalAppUser = userRepository.findByUsernameIgnoreCase(author.trim().toLowerCase());
        if (optionalAppUser.isEmpty()) {
            return new ArrayList<TaskResponseGetDTO>();
        }
        AppUser user = optionalAppUser.get();

        List<TaskEntity> taskEntities = (List<TaskEntity>) taskRepository.findAll();

        List<TaskResponseGetDTO> taskResponseDTOList = taskEntities.stream()
                .filter(task -> task.getAuthor().equals(user))
                .map(this::taskEntityToTaskResponseGetDTO)
                .collect(Collectors.toList());;

        Collections.reverse(taskResponseDTOList);

        return taskResponseDTOList;
    }

    public TaskResponseDTO taskEntityToTaskResponseDTO(TaskEntity taskEntity) {
        String assignee = "none";
        if (taskEntity.getAssignee() != null) {
            assignee = taskEntity.getAssignee().getUsername();
        }

        return new TaskResponseDTO(
                taskEntity.getId().toString(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getStatus().toString(),
                taskEntity.getAuthor().getUsername(),
                assignee
        );
    }

    public TaskResponseGetDTO taskEntityToTaskResponseGetDTO(TaskEntity taskEntity) {
        String assignee = "none";
        if (taskEntity.getAssignee() != null) {
            assignee = taskEntity.getAssignee().getUsername();
        }

        return new TaskResponseGetDTO(
                taskEntity.getId().toString(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getStatus().toString(),
                taskEntity.getAuthor().getUsername(),
                assignee,
                taskEntity.getComments().size()
        );
    }
}
