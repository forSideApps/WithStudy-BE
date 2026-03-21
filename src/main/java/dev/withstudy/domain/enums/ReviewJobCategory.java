package dev.withstudy.domain.enums;

public enum ReviewJobCategory {
    BACKEND("백엔드"),
    FRONTEND("프론트"),
    OTHER("기타");

    private final String displayName;

    ReviewJobCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
