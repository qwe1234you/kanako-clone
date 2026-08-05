package com.kanako.modules.treehole.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kanako.common.response.R;
import com.kanako.modules.treehole.entity.Treehole;
import com.kanako.modules.treehole.entity.TreeholeComment;
import com.kanako.modules.treehole.service.TreeholeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-common/treehole")
@RequiredArgsConstructor
public class TreeholeController {

    private final TreeholeService treeholeService;

    @GetMapping("/page")
    public R<Page<Treehole>> page(@RequestParam(defaultValue = "1") long page,
                                  @RequestParam(defaultValue = "12") long size) {
        return R.ok(treeholeService.page(page, size));
    }

    @GetMapping("/{id}")
    public R<Treehole> detail(@PathVariable Long id) {
        return R.ok(treeholeService.detail(id));
    }

    @PostMapping
    public R<Long> create(@RequestBody Map<String, String> body) {
        return R.ok(treeholeService.create(body));
    }

    @PostMapping("/{id}/like")
    public R<Integer> like(@PathVariable Long id) {
        return R.ok(treeholeService.like(id));
    }

    @GetMapping("/{id}/comments")
    public R<List<TreeholeComment>> comments(@PathVariable Long id) {
        return R.ok(treeholeService.comments(id));
    }

    @PostMapping("/{id}/comments")
    public R<Long> comment(@PathVariable Long id,
                           @RequestBody Map<String, String> body) {
        return R.ok(treeholeService.comment(id, body.get("nickname"), body.get("content")));
    }
}
