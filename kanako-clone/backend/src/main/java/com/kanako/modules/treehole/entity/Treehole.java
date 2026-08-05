package com.kanako.modules.treehole.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("treehole")
public class Treehole {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String nickname;
    private String avatar;
    private String content;
    private String mood;
    private String color;
    private Integer likeCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
}
