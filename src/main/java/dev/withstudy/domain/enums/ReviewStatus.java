package dev.withstudy.domain.enums;

public enum ReviewStatus {
    PENDING("검토전"),
    DONE("완료");

    private final String displayName;

    ReviewStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
