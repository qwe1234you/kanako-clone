package com.kanako.modules.site.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanako.common.response.R;
import com.kanako.modules.blog.entity.Blog;
import com.kanako.modules.blog.mapper.BlogMapper;
import com.kanako.modules.blog.vo.BlogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api-common/search")
@RequiredArgsConstructor
public class SearchController {

    private final BlogMapper blogMapper;

    @GetMapping
    public R<Map<String, Object>> search(@RequestParam(defaultValue = "") String q) {
        List<Blog> blogs = List.of();
        if (StringUtils.hasText(q)) {
            blogs = blogMapper.selectList(new LambdaQueryWrapper<Blog>()
                    .eq(Blog::getStatus, "PUBLISHED")
                    .and(w -> w.like(Blog::getTitle, q).or().like(Blog::getContentMd, q))
                    .orderByDesc(Blog::getCreatedAt)
                    .last("limit 20"));
        }
        List<Map<String, Object>> items = blogs.stream().map(b -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", b.getId());
            m.put("title", b.getTitle());
            m.put("slug", b.getSlug());
            m.put("summary", b.getSummary());
            m.put("createdAt", b.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        return R.ok(Map.of("blogs", items));
    }
}