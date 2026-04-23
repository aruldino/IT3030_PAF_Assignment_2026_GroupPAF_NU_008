package com.smartcampus.repository;

import com.smartcampus.model.TicketComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketCommentRepository extends MongoRepository<TicketComment, String> {
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(String ticketId);
}
