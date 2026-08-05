package com.kanako.modules.site.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanako.modules.blog.entity.Blog;
import com.kanako.modules.blog.mapper.BlogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RssController {

    private final BlogMapper blogMapper;

    private static final DateTimeFormatter RFC1123 = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("Asia/Shanghai"));

    @GetMapping(value = "/rss", produces = MediaType.APPLICATION_XML_VALUE)
    public String rss() {
        List<Blog> blogs = blogMapper.selectList(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getStatus, "PUBLISHED")
                .orderByDesc(Blog::getCreatedAt)
                .last("limit 20"));

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<rss version=\"2.0\">\n<channel>\n");
        sb.append("  <title>Kanako</title>\n");
        sb.append("  <link>https://blog.kanako.top</link>\n");
        sb.append("  <description>宁静致远，厚积薄发</description>\n");
        sb.append("  <language>zh-CN</language>\n");
        for (Blog b : blogs) {
            sb.append("  <item>\n");
            sb.append("    <title>").append(escapeXml(b.getTitle())).append("</title>\n");
            sb.append("    <link>https://blog.kanako.top/blogs/").append(escapeXml(b.getSlug())).append("</link>\n");
            sb.append("    <guid isPermaLink=\"true\">https://blog.kanako.top/blogs/").append(escapeXml(b.getSlug())).append("</guid>\n");
            if (b.getSummary() != null) {
                sb.append("    <description>").append(escapeXml(b.getSummary())).append("</description>\n");
            }
            if (b.getCreatedAt() != null) {
                sb.append("    <pubDate>").append(RFC1123.format(b.getCreatedAt().atZone(ZoneId.of("Asia/Shanghai")))).append("</pubDate>\n");
            }
            sb.append("  </item>\n");
        }
        sb.append("</channel>\n</rss>");
        return sb.toString();
    }

    private String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}