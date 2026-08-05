package com.kanako.modules.blog.controller;

import com.kanako.common.jwt.RequireAdmin;
import com.kanako.common.response.R;
import com.kanako.modules.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-common/blog")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{blogId}/comments")
    public R<List<Map<String, Object>>> list(@PathVariable Long blogId) {
        return R.ok(commentService.listByBlog(blogId));
    }

    @PostMapping("/{blogId}/comments")
    public R<Long> create(@PathVariable Long blogId,
                          @RequestParam(required = false) String nickname,
                          @RequestParam(required = false) String email,
                          @RequestParam String content,
                          @RequestParam(required = false) Long parentId) {
        return R.ok(commentService.create(blogId, nickname, email, content, parentId));
    }

    @DeleteMapping("/comments/{id}")
    @RequireAdmin
    public R<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return R.ok();
    }
}