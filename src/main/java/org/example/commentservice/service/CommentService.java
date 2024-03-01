package org.example.commentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.commentservice.model.Comment;
import org.example.commentservice.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment getById(String id) {
        return commentRepository.findById(id)
                .orElse(null);
    }

    public List<Comment> getAllByPostId(Long postId) {
        return commentRepository.findAllByPostId(postId);
    }

    public List<Comment> getAllByUserId(Long userId) {
        return commentRepository.findAllByUserId(userId);
    }

    public Comment create(Comment comment) {
        var commentCreated = commentRepository.save(comment);
        log.info("Comment created: {}", commentCreated);
        return commentCreated;
    }

    public void deleteById(String id) {
        log.info("Attempt to delete a comment by id '{}'", id);
        commentRepository.deleteById(id);
    }
}
