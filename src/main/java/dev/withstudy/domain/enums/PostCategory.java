package dev.withstudy.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCategory {
    SUGGESTION("건의 / 기능 요청"),
    FREE("자유게시판");

    private final String displayName;
}
