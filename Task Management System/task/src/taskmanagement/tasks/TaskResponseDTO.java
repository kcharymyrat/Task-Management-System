package taskmanagement.tasks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record TaskResponseDTO(
        @NotBlank
        @NotNull
        String id,

        @NotBlank
        @NotNull
        String title,

        @NotBlank
        @NotNull
        String description,

        @NotBlank
        @NotNull
        String status,

        @NotBlank
        @NotNull
        String author,

        String assignee
) { }
