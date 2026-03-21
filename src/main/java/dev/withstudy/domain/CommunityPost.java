package dev.withstudy.domain;

import dev.withstudy.domain.enums.PostCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "community_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_community_post")
    @SequenceGenerator(name = "seq_community_post", sequenceName = "SEQ_COMMUNITY_POST", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(name = "author_name", nullable = false, length = 50)
    private String authorName;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CommunityComment> comments = new ArrayList<>();

    public void incrementViewCount() {
        this.viewCount++;
    }
}
