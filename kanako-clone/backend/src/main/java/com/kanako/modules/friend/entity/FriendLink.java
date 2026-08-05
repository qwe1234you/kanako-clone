package com.kanako.modules.friend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("friend_link")
public class FriendLink {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String url;
    private String avatar;
    private String description;
    private Integer sort;
    /** SHOW / HIDE / PENDING */
    private String status;
    private LocalDateTime createdAt;
}
