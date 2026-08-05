package com.kanako.modules.site.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanako.common.response.R;
import com.kanako.modules.blog.entity.Blog;
import com.kanako.modules.blog.entity.BlogComment;
import com.kanako.modules.blog.mapper.BlogCommentMapper;
import com.kanako.modules.blog.mapper.BlogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api-common/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final BlogMapper blogMapper;
    private final BlogCommentMapper commentMapper;

    @GetMapping("/summary")
    public R<Map<String, Object>> summary() {
        Map<String, Object> m = new HashMap<>();
        m.put("blogCount", blogMapper.selectCount(new LambdaQueryWrapper<Blog>().eq(Blog::getStatus, "PUBLISHED")));
        m.put("commentCount", commentMapper.selectCount(null));
        long views = blogMapper.selectList(new LambdaQueryWrapper<Blog>().eq(Blog::getStatus, "PUBLISHED"))
                .stream().mapToLong(b -> b.getViewCount() == null ? 0 : b.getViewCount()).sum();
        m.put("viewCount", views);
        return R.ok(m);
    }
}