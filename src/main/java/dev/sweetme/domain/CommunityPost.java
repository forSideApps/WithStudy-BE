package dev.sweetme.domain;

import dev.sweetme.domain.enums.PostCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "member_username", length = 50)
    private String memberUsername;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Formula("(SELECT COUNT(*) FROM community_comment c WHERE c.post_id = id)")
    private int commentCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<CommunityComment> comments = new ArrayList<>();

    public void incrementViewCount() {
        this.viewCount++;
    }
}
