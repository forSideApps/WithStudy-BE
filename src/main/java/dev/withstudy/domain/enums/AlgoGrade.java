package dev.withstudy.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlgoGrade {
    UNRANKED("언레이티드"),
    BRONZE("브론즈"),
    SILVER("실버"),
    GOLD("골드"),
    PLATINUM("플래티넘"),
    DIAMOND("다이아몬드"),
    RUBY("루비"),
    MASTER("마스터");

    private final String displayName;
}
