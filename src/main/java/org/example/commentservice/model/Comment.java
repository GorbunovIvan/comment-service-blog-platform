package org.example.commentservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collation = "comments")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
@ToString
public class Comment {

    @Id
    @EqualsAndHashCode.Exclude
    private String id;

    private Long postId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
