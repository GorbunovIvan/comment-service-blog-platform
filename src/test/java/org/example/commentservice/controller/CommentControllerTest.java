package org.example.commentservice.controller;

import org.example.commentservice.model.Comment;
import org.example.commentservice.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommentControllerTest {

    private HttpGraphQlTester graphQlTester;

    @MockBean
    private CommentService commentService;

    private List<Comment> commentsInDB;
    
    @BeforeEach
    void setUp(@Autowired ApplicationContext applicationContext) {

        if (graphQlTester == null) {
            var webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                    .configureClient()
                    .baseUrl("/graphql")
                    .build();
            graphQlTester = HttpGraphQlTester.create(webTestClient);
        }

        commentsInDB = new ArrayList<>(List.of(
                new Comment("1", 2L, 3L, "first comment", LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS)),
                new Comment("2", 1L, 1L, "second comment", LocalDateTime.now().minusWeeks(1).truncatedTo(ChronoUnit.SECONDS)),
                new Comment("3", 1L, 2L, "third comment", LocalDateTime.now().minusYears(1).truncatedTo(ChronoUnit.SECONDS)),
                new Comment("4", 3L, 4L, "fourth comment", LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.SECONDS)),
                new Comment("5", 2L, 3L, "fifth comment", LocalDateTime.now().minusDays(5).truncatedTo(ChronoUnit.SECONDS))
        ));

        // Mocking
        when(commentService.getById("-1")).thenReturn(null);
        when(commentService.getAllByPostId(-1L)).thenReturn(Collections.emptyList());
        when(commentService.getAllByUserId(-1L)).thenReturn(Collections.emptyList());
        when(commentService.create(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId("99");
            return comment;
        });

        for (var comment : commentsInDB) {
            when(commentService.getById(comment.getId())).thenReturn(comment);
        }

        var postIds = commentsInDB.stream().map(Comment::getPostId).distinct().toList();
        for (var postId : postIds) {
            var commentsByPostId = commentsInDB.stream().filter(c -> c.getPostId().equals(postId)).toList();
            when(commentService.getAllByPostId(postId)).thenReturn(commentsByPostId);
        }

        var userIds = commentsInDB.stream().map(Comment::getUserId).distinct().toList();
        for (var userId : userIds) {
            var commentsByUserId = commentsInDB.stream().filter(c -> c.getUserId().equals(userId)).toList();
            when(commentService.getAllByUserId(userId)).thenReturn(commentsByUserId);
        }
    }

    @Test
    void testGetById() {

        for (var comment : commentsInDB) {

            String query = """
                {
                  getById(id: "%s") {
                    id
                    postId
                    userId
                    content
                    createdAt
                  }
                }
                """;

            query = String.format(query, comment.getId());

            graphQlTester.document(query)
                    .execute()
                    .path("data.getById")
                    .hasValue()
                    .entity(Comment.class)
                    .isEqualTo(comment);

            verify(commentService, times(1)).getById(comment.getId());
        }

        verify(commentService, times(commentsInDB.size())).getById(anyString());
    }

    @Test
    void testGetById_NotFound() {

        var id = "-1L";

        String query = """
                {
                  getById(id: "%s") {
                    id
                    postId
                    userId
                    content
                    createdAt
                  }
                }
                """;

        query = String.format(query, id);

        graphQlTester.document(query)
                .execute()
                .path("data.getById")
                .valueIsNull();

        verify(commentService, times(1)).getById(id);
    }

    @Test
    void testGetAllByPostId() {

        var postIds = commentsInDB.stream().map(Comment::getPostId).distinct().toList();

        for (var postId : postIds) {

            String query = """
                {
                  getAllByPostId(postId: %d) {
                    id
                    postId
                    userId
                    content
                    createdAt
                  }
                }
                """;

            query = String.format(query, postId);

            var result = graphQlTester.document(query)
                    .execute()
                    .path("data.getAllByPostId")
                    .hasValue()
                    .entityList(Comment.class)
                    .get();

            var resultExpected = commentsInDB.stream().filter(c -> c.getPostId().equals(postId)).toList();
            assertEquals(new HashSet<>(resultExpected), new HashSet<>(result));
            verify(commentService, times(1)).getAllByPostId(postId);
        }

        verify(commentService, times(postIds.size())).getAllByPostId(anyLong());
    }

    @Test
    void testGetAllByPostId_NotFound() {

        var postId = -1L;

        String query = """
                {
                  getAllByPostId(postId: %d) {
                    id
                    postId
                    userId
                    content
                    createdAt
                  }
                }
                """;

        query = String.format(query, postId);

        graphQlTester.document(query)
                .execute()
                .path("data.getAllByPostId")
                .hasValue()
                .entityList(Comment.class)
                .hasSize(0);

        verify(commentService, times(1)).getAllByPostId(postId);
    }

    @Test
    void testGetAllByUserId() {

        var userIds = commentsInDB.stream().map(Comment::getUserId).distinct().toList();

        for (var userId : userIds) {

            String query = """
                {
                  getAllByUserId(userId: %d) {
                    id
                    postId
                    userId
                    content
                    createdAt
                  }
                }
                """;

            query = String.format(query, userId);

            var result = graphQlTester.document(query)
                    .execute()
                    .path("data.getAllByUserId")
                    .hasValue()
                    .entityList(Comment.class)
                    .get();

            var resultExpected = commentsInDB.stream().filter(c -> c.getUserId().equals(userId)).toList();
            assertEquals(new HashSet<>(resultExpected), new HashSet<>(result));
            verify(commentService, times(1)).getAllByUserId(userId);
        }

        verify(commentService, times(userIds.size())).getAllByUserId(anyLong());
    }

    @Test
    void testGetAllByUserId_NotFound() {

        var userId = -1L;

        String query = """
                {
                  getAllByUserId(userId: %d) {
                    id
                    postId
                    userId
                    content
                    createdAt
                  }
                }
                """;

        query = String.format(query, userId);

        graphQlTester.document(query)
                .execute()
                .path("data.getAllByUserId")
                .hasValue()
                .entityList(Comment.class)
                .hasSize(0);

        verify(commentService, times(1)).getAllByUserId(userId);
    }

    @Test
    void testCreate() {

        var newComment = new Comment(null, 4L, 2L, "new comment", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        String query = """
                mutation {
                  create(postId: %d, userId: %d, content: "%s", createdAt: "%s") {
                    id
                    postId
                    userId
                    content
                    createdAt
                  }
                }
                """;

        query = String.format(query,
                newComment.getPostId(),
                newComment.getUserId(),
                newComment.getContent(),
                DateTimeFormatter.ISO_DATE_TIME.format(newComment.getCreatedAt()));

        graphQlTester.document(query)
                .execute()
                .path("data.create")
                .hasValue()
                .entity(Comment.class)
                .isEqualTo(newComment);

        verify(commentService, times(1)).create(any(Comment.class));
    }

    @Test
    void testCreate_WithoutCreatedAt() {

        var newComment = new Comment(null, 4L, 2L, "new comment", null);

        String query = """
                mutation {
                  create(postId: %d, userId: %d, content: "%s") {
                    id
                    postId
                    userId
                    content
                    createdAt
                  }
                }
                """;

        query = String.format(query,
                newComment.getPostId(),
                newComment.getUserId(),
                newComment.getContent());

        var result = graphQlTester.document(query)
                .execute()
                .path("data.create")
                .hasValue()
                .entity(Comment.class)
                .get();

        assertEquals(newComment.getPostId(), result.getPostId());
        assertEquals(newComment.getUserId(), result.getUserId());
        assertEquals(newComment.getContent(), result.getContent());
        assertNotNull(result.getCreatedAt());

        verify(commentService, times(1)).create(any(Comment.class));
    }

    @Test
    void testDeleteById() {

        for (var comment : commentsInDB) {

            String query = """
                mutation {
                  deleteById(id: "%s")
                }
                """;

            query = String.format(query, comment.getId());

            graphQlTester.document(query)
                    .execute()
                    .path("data.deleteById")
                    .hasValue()
                    .entity(String.class)
                    .isEqualTo(comment.getId());

            verify(commentService, times(1)).deleteById(comment.getId());
        }

        verify(commentService, times(commentsInDB.size())).deleteById(anyString());
    }
}