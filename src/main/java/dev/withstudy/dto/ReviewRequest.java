package dev.withstudy.dto;

import lombok.Getter;

@Getter
public class ReviewRequest {
    private String type;
    private String jobCategory;
    private String careerLevel;
    private String title;
    private String content;
    private String authorName;
    private String contactInfo;
    private String password;
}
