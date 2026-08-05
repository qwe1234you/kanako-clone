package com.kanako.modules.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanako.common.exception.BusinessException;
import com.kanako.common.jwt.UserContext;
import com.kanako.modules.blog.entity.Blog;
import com.kanako.modules.blog.entity.BlogComment;
import com.kanako.modules.blog.mapper.BlogCommentMapper;
import com.kanako.modules.blog.mapper.BlogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final BlogCommentMapper commentMapper;
    private final BlogMapper blogMapper;

    public List<Map<String, Object>> listByBlog(Long blogId) {
        List<BlogComment> list = commentMapper.selectList(new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getBlogId, blogId)
                .orderByAsc(BlogComment::getCreatedAt));
        return list.stream().map(c -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", c.getId());
            m.put("nickname", c.getNickname());
            m.put("email", c.getEmail());
            m.put("avatar", c.getAvatar() == null ? "" : c.getAvatar());
            m.put("content", c.getContent());
            m.put("parentId", c.getParentId());
            m.put("isAdmin", c.getIsAdmin());
            m.put("createdAt", c.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
    }

    public Long create(Long blogId, String nickname, String email, String content, Long parentId) {
        if (blogMapper.selectById(blogId) == null) {
            throw new BusinessException(404, "文章不存在");
        }
        if (!StringUtils.hasText(content) || content.trim().length() > 2000) {
            throw new BusinessException("评论内容不能为空且不超过 2000 字");
        }
        BlogComment c = new BlogComment();
        c.setBlogId(blogId);
        c.setNickname(StringUtils.hasText(nickname) ? nickname.trim() : "匿名");
        c.setEmail(StringUtils.hasText(email) ? email.trim() : null);
        c.setContent(content.trim());
        c.setParentId(parentId == null ? 0L : parentId);
        c.setIsAdmin(UserContext.isAdmin() ? 1 : 0);
        c.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(c);
        return c.getId();
    }

    public void delete(Long id) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限");
        }
        commentMapper.deleteById(id);
    }

    public long count() {
        return commentMapper.selectCount(null);
    }
}