package dev.withstudy.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomStatus {
    OPEN("모집중"),
    FULL("마감"),
    CLOSED("종료");

    private final String displayName;
}
