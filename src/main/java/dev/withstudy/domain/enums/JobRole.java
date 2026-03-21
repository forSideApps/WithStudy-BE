package dev.withstudy.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobRole {
    BACKEND("백엔드"),
    FRONTEND("프론트엔드"),
    FULLSTACK("풀스택"),
    MOBILE("모바일"),
    AI_ML("AI/ML"),
    DATA("데이터"),
    DEVOPS("DevOps/인프라"),
    SECURITY("보안"),
    EMBEDDED("임베디드"),
    OTHER("기타");

    private final String displayName;
}
