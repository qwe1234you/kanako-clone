package com.kanako.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private String email;
    private String avatar;
    private String role;
    private Long githubId;
    private String githubLogin;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonProperty("nickname")
    public String getNickname() {
        return username;
    }
}