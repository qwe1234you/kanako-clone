package com.kanako.modules.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("blog_post_tag")
public class BlogPostTag {

    private Long blogId;
    private Long tagId;
}