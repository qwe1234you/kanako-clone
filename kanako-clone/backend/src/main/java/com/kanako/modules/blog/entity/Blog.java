package com.kanako.modules.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog")
public class Blog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String slug;
    private String summary;
    private String cover;
    private String contentMd;
    private Long categoryId;

    /** PUBLISHED / DRAFT */
    private String status;
    private Long viewCount;
    private Integer likeCount;
    private Integer isTop;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}