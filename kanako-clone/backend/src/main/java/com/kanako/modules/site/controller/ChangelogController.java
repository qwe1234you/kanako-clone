package com.kanako.modules.site.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanako.common.response.R;
import com.kanako.modules.site.entity.Changelog;
import com.kanako.modules.site.mapper.ChangelogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api-common/update")
@RequiredArgsConstructor
public class ChangelogController {

    private final ChangelogMapper changelogMapper;

    @GetMapping("/list")
    public R<List<Changelog>> list() {
        return R.ok(changelogMapper.selectList(new LambdaQueryWrapper<Changelog>()
                .orderByDesc(Changelog::getDate).orderByDesc(Changelog::getId)));
    }
}
