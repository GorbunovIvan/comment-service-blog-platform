package org.example.commentservice.service;

import lombok.RequiredArgsConstructor;
import org.example.commentservice.model.Comment;
import org.example.commentservice.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
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
        return commentRepository.save(comment);
    }

    public void deleteById(String id) {
        commentRepository.deleteById(id);
    }
}
