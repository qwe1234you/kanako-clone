package com.kanako.modules.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kanako.common.jwt.RequireAdmin;
import com.kanako.common.response.R;
import com.kanako.modules.blog.dto.BlogDTO;
import com.kanako.modules.blog.service.BlogService;
import com.kanako.modules.blog.vo.BlogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-common/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    /** 公开分页列表 */
    @GetMapping("/page")
    public R<Page<BlogVO>> page(@RequestParam(defaultValue = "1") long page,
                                @RequestParam(defaultValue = "10") long size,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String tag,
                                @RequestParam(required = false) String keyword) {
        return R.ok(blogService.pagePublic(page, size, category, tag, keyword));
    }

    /** 管理端分页（含草稿） */
    @GetMapping("/admin/page")
    @RequireAdmin
    public R<Page<BlogVO>> adminPage(@RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "10") long size,
                                     @RequestParam(required = false) String status) {
        return R.ok(blogService.pageAdmin(page, size, status));
    }

    /** 公开详情（slug 或数字 id） */
    @GetMapping("/{slugOrId}")
    public R<BlogVO> detail(@PathVariable String slugOrId) {
        return R.ok(blogService.detail(slugOrId));
    }

    /** 管理端详情 */
    @GetMapping("/admin/{id}")
    @RequireAdmin
    public R<BlogVO> adminDetail(@PathVariable Long id) {
        return R.ok(blogService.adminDetail(id));
    }

    @PostMapping
    @RequireAdmin
    public R<Long> create(@RequestBody BlogDTO dto) {
        return R.ok(blogService.create(dto));
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public R<Void> update(@PathVariable Long id, @RequestBody BlogDTO dto) {
        blogService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public R<Void> delete(@PathVariable Long id) {
        blogService.delete(id);
        return R.ok();
    }

    /** 分类列表（带计数） */
    @GetMapping("/categories")
    public R<List<Map<String, Object>>> categories() {
        return R.ok(blogService.categoriesWithCount());
    }

    @PostMapping("/categories")
    @RequireAdmin
    public R<Void> saveCategory(@RequestParam(required = false) Long id,
                                @RequestParam String name,
                                @RequestParam String slug,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) Integer sort) {
        blogService.createCategory(id, name, slug, description, sort);
        return R.ok();
    }

    @DeleteMapping("/categories/{id}")
    @RequireAdmin
    public R<Void> deleteCategory(@PathVariable Long id) {
        blogService.deleteCategory(id);
        return R.ok();
    }

    /** 标签列表（带计数） */
    @GetMapping("/tags")
    public R<List<Map<String, Object>>> tags() {
        return R.ok(blogService.tagsWithCount());
    }

    @PostMapping("/tags")
    @RequireAdmin
    public R<Void> saveTag(@RequestParam(required = false) Long id,
                           @RequestParam String name,
                           @RequestParam String slug) {
        if (id == null) {
            blogService.createTag(name, slug);
        } else {
            blogService.updateTag(id, name, slug);
        }
        return R.ok();
    }

    @DeleteMapping("/tags/{id}")
    @RequireAdmin
    public R<Void> deleteTag(@PathVariable Long id) {
        blogService.deleteTag(id);
        return R.ok();
    }

    /** 归档数据 */
    @GetMapping("/archive")
    public R<Map<String, Object>> archive() {
        return R.ok(blogService.archive());
    }
}