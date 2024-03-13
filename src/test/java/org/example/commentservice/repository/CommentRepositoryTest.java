package org.example.commentservice.repository;

import org.example.commentservice.BaseIntegrationTest;
import org.example.commentservice.model.Comment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    private List<Comment> commentsInDB;

    private boolean clearCollectionAfterEachTest = false;

    @BeforeEach
    void setUp() {

        clearCollectionAfterEachTest = false;

        assertTrue(commentRepository.findAll().isEmpty(), "The test database collection must be empty. Maybe this is not a test base???");

        commentsInDB = new ArrayList<>(List.of(
                commentRepository.save(new Comment(null, 2L, 3L, "first comment", LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS))),
                commentRepository.save(new Comment(null, 1L, 1L, "second comment", LocalDateTime.now().minusWeeks(1).truncatedTo(ChronoUnit.SECONDS))),
                commentRepository.save(new Comment(null, 1L, 2L, "third comment", LocalDateTime.now().minusYears(1).truncatedTo(ChronoUnit.SECONDS))),
                commentRepository.save(new Comment(null, 3L, 4L, "fourth comment", LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.SECONDS))),
                commentRepository.save(new Comment(null, 2L, 3L, "fifth comment", LocalDateTime.now().minusDays(5).truncatedTo(ChronoUnit.SECONDS)))
        ));

        assertFalse(commentsInDB.isEmpty());
        assertEquals(commentsInDB.size(), commentRepository.findAll().size());

        clearCollectionAfterEachTest = true;
    }

    @AfterEach
    void tearDown() {
        if (clearCollectionAfterEachTest) {
            commentRepository.deleteAll();
        }
    }

    @Test
    void findAllByPostId() {

        var postIds = commentsInDB.stream().map(Comment::getPostId).distinct().toList();

        for (var postId : postIds) {
            var resultExpected = commentsInDB.stream().filter(c -> c.getPostId().equals(postId)).toList();
            var result = commentRepository.findAllByPostId(postId);
            assertEquals(new HashSet<>(resultExpected), new HashSet<>(result));
        }
    }

    @Test
    void findAllByPostId_NotFound() {
        var result = commentRepository.findAllByPostId(-1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByUserId() {

        var userIds = commentsInDB.stream().map(Comment::getUserId).distinct().toList();

        for (var userId : userIds) {
            var resultExpected = commentsInDB.stream().filter(c -> c.getUserId().equals(userId)).toList();
            var result = commentRepository.findAllByUserId(userId);
            assertEquals(new HashSet<>(resultExpected), new HashSet<>(result));
        }
    }

    @Test
    void findAllByUserId_NotFound() {
        var result = commentRepository.findAllByUserId(-1L);
        assertTrue(result.isEmpty());
    }
}