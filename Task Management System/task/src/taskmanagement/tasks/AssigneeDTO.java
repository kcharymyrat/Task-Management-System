package taskmanagement.tasks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssigneeDTO(@NotBlank @NotNull String assignee) { }
