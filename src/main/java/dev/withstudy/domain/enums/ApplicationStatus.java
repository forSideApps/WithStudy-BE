package dev.withstudy.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {
    PENDING("대기중"),
    APPROVED("승인"),
    REJECTED("거절");

    private final String displayName;
}
