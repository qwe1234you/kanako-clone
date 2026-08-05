package com.kanako.modules.blog.vo;

import com.kanako.modules.blog.entity.BlogCategory;
import com.kanako.modules.blog.entity.BlogTag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class BlogVO {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String cover;
    /** 仅详情接口返回正文，列表接口不填充 */
    private String contentMd;
    private Long categoryId;
    private BlogCategory category;
    private List<BlogTag> tags = new ArrayList<>();
    private String status;
    private Long viewCount;
    private Integer likeCount;
    private Integer isTop;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}