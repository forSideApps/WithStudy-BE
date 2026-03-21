package dev.withstudy.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_community_comment")
    @SequenceGenerator(name = "seq_community_comment", sequenceName = "SEQ_COMMUNITY_COMMENT", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "author_name", nullable = false, length = 50)
    private String authorName;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
