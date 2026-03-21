package dev.withstudy.dto;

import lombok.Getter;

@Getter
public class ReviewUpdateRequest {
    private String type;
    private String jobCategory;
    private String careerLevel;
    private String title;
    private String content;
    private String contactInfo;
}
