package org.example.commentservice.service;

import org.example.commentservice.model.Comment;
import org.example.commentservice.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureDataMongo
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @MockBean
    private CommentRepository commentRepository;

    private List<Comment> commentsInDB;

    @BeforeEach
    void setUp() {

        commentsInDB = new ArrayList<>(List.of(
                new Comment("1", 2L, 3L, "first comment", LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS)),
                new Comment("2", 1L, 1L, "second comment", LocalDateTime.now().minusWeeks(1).truncatedTo(ChronoUnit.SECONDS)),
                new Comment("3", 1L, 2L, "third comment", LocalDateTime.now().minusYears(1).truncatedTo(ChronoUnit.SECONDS)),
                new Comment("4", 3L, 4L, "fourth comment", LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.SECONDS)),
                new Comment("5", 2L, 3L, "fifth comment", LocalDateTime.now().minusDays(5).truncatedTo(ChronoUnit.SECONDS))
        ));

        // Mocking
        when(commentRepository.findById("-1")).thenReturn(Optional.empty());
        when(commentRepository.findAllByPostId(-1L)).thenReturn(Collections.emptyList());
        when(commentRepository.findAllByUserId(-1L)).thenReturn(Collections.emptyList());
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId("99");
            return comment;
        });

        for (var comment : commentsInDB) {
            when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        }

        var postIds = commentsInDB.stream().map(Comment::getPostId).distinct().toList();
        for (var postId : postIds) {
            var commentsByPostId = commentsInDB.stream().filter(c -> c.getPostId().equals(postId)).toList();
            when(commentRepository.findAllByPostId(postId)).thenReturn(commentsByPostId);
        }

        var userIds = commentsInDB.stream().map(Comment::getUserId).distinct().toList();
        for (var userId : userIds) {
            var commentsByUserId = commentsInDB.stream().filter(c -> c.getUserId().equals(userId)).toList();
            when(commentRepository.findAllByUserId(userId)).thenReturn(commentsByUserId);
        }
    }

    @Test
    void testGetById() {
        for (var comment : commentsInDB) {
            var result = commentService.getById(comment.getId());
            assertEquals(comment, result);
            verify(commentRepository, times(1)).findById(comment.getId());
        }
        verify(commentRepository, times(commentsInDB.size())).findById(anyString());
    }

    @Test
    void testGetById_NotFound() {
        var id = "-1L";
        var result = commentService.getById(id);
        assertNull(result);
        verify(commentRepository, times(1)).findById(id);
    }

    @Test
    void testGetAllByPostId() {

        var postIds = commentsInDB.stream().map(Comment::getPostId).distinct().toList();

        for (var postId : postIds) {
            var resultExpected = commentsInDB.stream().filter(c -> c.getPostId().equals(postId)).toList();
            var result = commentService.getAllByPostId(postId);
            assertEquals(new HashSet<>(resultExpected), new HashSet<>(result));
            verify(commentRepository, times(1)).findAllByPostId(postId);
        }

        verify(commentRepository, times(postIds.size())).findAllByPostId(anyLong());
    }

    @Test
    void testGetAllByPostId_NotFound() {
        var postId = -1L;
        var result = commentService.getAllByPostId(postId);
        assertTrue(result.isEmpty());
        verify(commentRepository, times(1)).findAllByPostId(postId);
    }

    @Test
    void testGetAllByUserId() {

        var userIds = commentsInDB.stream().map(Comment::getUserId).distinct().toList();

        for (var userId : userIds) {
            var resultExpected = commentsInDB.stream().filter(c -> c.getUserId().equals(userId)).toList();
            var result = commentService.getAllByUserId(userId);
            assertEquals(new HashSet<>(resultExpected), new HashSet<>(result));
            verify(commentRepository, times(1)).findAllByUserId(userId);
        }

        verify(commentRepository, times(userIds.size())).findAllByUserId(anyLong());
    }

    @Test
    void testGetAllByUserId_NotFound() {
        var userId = -1L;
        var result = commentService.getAllByUserId(userId);
        assertTrue(result.isEmpty());
        verify(commentRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    void testCreate() {

        var newComment = new Comment(null, 4L, 2L, "new comment", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        var result = commentService.create(newComment);
        assertEquals(newComment, result);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testDeleteById() {
        for (var comment : commentsInDB) {
            commentService.deleteById(comment.getId());
            verify(commentRepository, times(1)).deleteById(comment.getId());
        }
        verify(commentRepository, times(commentsInDB.size())).deleteById(anyString());
    }
}