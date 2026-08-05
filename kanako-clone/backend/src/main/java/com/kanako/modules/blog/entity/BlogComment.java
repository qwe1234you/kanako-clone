package com.kanako.modules.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_comment")
public class BlogComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long blogId;
    private Long userId;
    private String nickname;
    private String email;
    private String avatar;
    private String content;
    private Long parentId;
    private Integer isAdmin;
    private LocalDateTime createdAt;
}