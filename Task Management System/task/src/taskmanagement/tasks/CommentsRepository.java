package taskmanagement.tasks;

import org.springframework.data.repository.CrudRepository;

public interface CommentsRepository extends CrudRepository<CommentEntity, Long> {
}
