package com.kanako.modules.blog.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BlogDTO {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String cover;
    private String contentMd;
    private Long categoryId;
    private List<Long> tagIds = new ArrayList<>();
    /** PUBLISHED / DRAFT */
    private String status;
    private Integer isTop;
}