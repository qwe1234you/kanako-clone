package com.kanako.modules.treehole.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("treehole_comment")
public class TreeholeComment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long holeId;
    private String nickname;
    private String avatar;
    private String content;
    private LocalDateTime createdAt;
}
