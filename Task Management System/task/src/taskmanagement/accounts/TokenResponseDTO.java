package taskmanagement.accounts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TokenResponseDTO(
        @NotNull
        @NotBlank
        String token
) { }
