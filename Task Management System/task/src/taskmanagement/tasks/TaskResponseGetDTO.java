package taskmanagement.tasks;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskResponseGetDTO(
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

        String assignee,

        @Column(name = "total_comments")
        int total_comments
) { }
