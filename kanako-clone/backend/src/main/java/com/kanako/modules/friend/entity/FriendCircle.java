package com.kanako.modules.friend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("friend_circle")
public class FriendCircle {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long linkId;
    private String content;
    /** 逗号分隔的图片地址 */
    private String images;
    private LocalDateTime createdAt;
}
