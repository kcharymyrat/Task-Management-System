package taskmanagement.tasks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskDTO(
        @NotBlank
        @NotNull
        String title,

        @NotNull
        @NotBlank
        String description
) { }
