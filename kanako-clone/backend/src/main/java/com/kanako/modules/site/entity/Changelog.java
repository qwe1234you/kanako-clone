package com.kanako.modules.site.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("changelog")
public class Changelog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String version;
    private String date;
    private String contentMd;
    private LocalDateTime createdAt;
}
