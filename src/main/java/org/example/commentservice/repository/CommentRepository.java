package org.example.commentservice.repository;

import org.example.commentservice.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findAllByPostId(@Param("postId") Long postId);
    List<Comment> findAllByUserId(@Param("userId") Long userId);
}
