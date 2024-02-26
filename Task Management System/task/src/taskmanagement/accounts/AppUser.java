package taskmanagement.accounts;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import taskmanagement.tasks.TaskEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "app_users", uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    private String username;

    @NotNull
    @NotBlank
    private String password;
    private String authority = "ROLE_USER";

    @OneToMany(mappedBy = "author")
    private Set<TaskEntity> tasks = new HashSet<>();

    @Column(name = "assigned_task")
    @OneToMany(mappedBy = "assignee")
    private Set<TaskEntity> assignedTasks = new HashSet<>();

    public AppUser() {
    }

    public Long getId() {
        return id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String email) {
        this.username = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Set<TaskEntity> getTasks() {
        return tasks;
    }

    public void addTask(TaskEntity task) {
        this.tasks.add(task);
    }

    public Set<TaskEntity> getAssignedTasks() {
        return assignedTasks;
    }

    public void addAssignedTask(TaskEntity task) {
        this.assignedTasks.add(task);
    }
}
