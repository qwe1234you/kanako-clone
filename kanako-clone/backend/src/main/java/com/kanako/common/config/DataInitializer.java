package com.kanako.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 首次启动初始化基础数据（幂等）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        initAdmin();
        initCategories();
        initTags();
        initSiteConfig();
        initFriendLinks();
        initChangelog();
    }

    private void initAdmin() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Long.class);
        if (count != null && count > 0) {
            return;
        }
        String hash = new BCryptPasswordEncoder().encode("admin123");
        jdbcTemplate.update(
                "INSERT INTO sys_user (username, password, email, avatar, role, created_at, updated_at) VALUES (?,?,?,?,?,?,?)",
                "admin", hash, "admin@kanako.local", "", "ADMIN",
                LocalDateTime.now(), LocalDateTime.now());
        log.info("已创建管理员账号 admin / admin123（请尽快修改密码）");
    }

    private void initCategories() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM blog_category", Long.class);
        if (count != null && count > 0) {
            return;
        }
        List<String[]> categories = List.of(
                new String[]{"技术", "tech", "编程、开发与折腾记录"},
                new String[]{"随笔", "life", "生活随想与碎碎念"},
                new String[]{"区块链", "blockchain", "链上笔记"});
        for (String[] c : categories) {
            jdbcTemplate.update(
                    "INSERT INTO blog_category (name, slug, description, sort, created_at) VALUES (?,?,?,?,?)",
                    c[0], c[1], c[2], 0, LocalDateTime.now());
        }
    }

    private void initTags() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM blog_tag", Long.class);
        if (count != null && count > 0) {
            return;
        }
        List<String[]> tags = List.of(
                new String[]{"Java", "java"},
                new String[]{"前端", "frontend"},
                new String[]{"随笔", "life"},
                new String[]{"区块链", "blockchain"},
                new String[]{"AI", "ai"});
        for (String[] t : tags) {
            jdbcTemplate.update("INSERT INTO blog_tag (name, slug, created_at) VALUES (?,?,?)",
                    t[0], t[1], LocalDateTime.now());
        }
    }

    private void initFriendLinks() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM friend_link", Long.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update("INSERT INTO friend_link (name, url, avatar, description, sort, status, created_at) VALUES (?,?,?,?,?,?,?)",
                "示例博客", "https://example.com", "", "一个示例友链", 0, "SHOW", LocalDateTime.now());
        jdbcTemplate.update("INSERT INTO friend_link (name, url, avatar, description, sort, status, created_at) VALUES (?,?,?,?,?,?,?)",
                "Kanako 原站", "https://blog.kanako.top", "", "树洞、博客与日常", 1, "SHOW", LocalDateTime.now());
    }

    private void initChangelog() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM changelog", Long.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update("INSERT INTO changelog (version, date, content_md, created_at) VALUES (?,?,?,?)",
                "v1.0.0", "2026-08-05", "- 博客：发布 / 编辑 / 归档 / 分类标签\n- 树洞：匿名倾诉、评论、点赞\n- 友链申请与朋友圈\n- AI 助手（可接入 OpenAI 兼容接口）\n- 工具页：JSON / 正则 / 时间戳", LocalDateTime.now());
        jdbcTemplate.update("INSERT INTO changelog (version, date, content_md, created_at) VALUES (?,?,?,?)",
                "v0.1.0", "2026-07-01", "站点初始化，搭建前后端骨架。", LocalDateTime.now());
    }

    private void initSiteConfig() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM site_config", Long.class);
        if (count != null && count > 0) {
            return;
        }
        Map<String, String> configs = Map.of(
                "site_name", "Kanako · Space",
                "site_description", "宁静致远，厚积薄发。",
                "site_icp", "",
                "site_footer", "© Kanako Clone");
        for (Map.Entry<String, String> e : configs.entrySet()) {
            jdbcTemplate.update(
                    "INSERT INTO site_config (config_key, config_value, updated_at) VALUES (?,?,?)",
                    e.getKey(), e.getValue(), LocalDateTime.now());
        }
    }
}