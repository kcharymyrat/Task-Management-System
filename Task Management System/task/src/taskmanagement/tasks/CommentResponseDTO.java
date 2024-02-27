package taskmanagement.tasks;

public record CommentResponseDTO(
        String id,
        String task_id,
        String text,
        String author
) { }
