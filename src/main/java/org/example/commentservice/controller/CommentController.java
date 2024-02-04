package org.example.commentservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.commentservice.model.Comment;
import org.example.commentservice.service.CommentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @QueryMapping()
    public Comment getById(@Argument String id) {
        return commentService.getById(id);
    }

    @QueryMapping
    public List<Comment> getAllByPostId(@Argument Long postId) {
        return commentService.getAllByPostId(postId);
    }

    @QueryMapping
    public List<Comment> getAllByUserId(@Argument Long userId) {
        return commentService.getAllByUserId(userId);
    }

    @MutationMapping
    public Comment create(@Argument Long postId, @Argument Long userId, @Argument String content,
                          @Argument @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAt) {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        var comment = new Comment(null, postId, userId, content, createdAt);
        return commentService.create(comment);
    }

    @MutationMapping
    public String deleteById(@Argument String id) {
        commentService.deleteById(id);
        return id;
    }
}
