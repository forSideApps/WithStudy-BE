package dev.withstudy.domain.enums;

public enum ReviewType {
    PORTFOLIO("포트폴리오"),
    RESUME("이력서");

    private final String displayName;

    ReviewType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
