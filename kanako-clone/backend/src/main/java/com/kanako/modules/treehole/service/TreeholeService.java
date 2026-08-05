package com.kanako.modules.treehole.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kanako.common.exception.BusinessException;
import com.kanako.modules.treehole.entity.Treehole;
import com.kanako.modules.treehole.entity.TreeholeComment;
import com.kanako.modules.treehole.mapper.TreeholeCommentMapper;
import com.kanako.modules.treehole.mapper.TreeholeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TreeholeService {

    private final TreeholeMapper holeMapper;
    private final TreeholeCommentMapper commentMapper;

    public Page<Treehole> page(long page, long size) {
        return holeMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Treehole>()
                        .orderByDesc(Treehole::getCreatedAt));
    }

    public Treehole detail(Long id) {
        Treehole hole = holeMapper.selectById(id);
        if (hole == null) {
            throw new BusinessException(404, "树洞不存在");
        }
        holeMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Treehole>()
                .eq(Treehole::getId, id)
                .setSql("view_count = view_count + 1"));
        hole.setViewCount((hole.getViewCount() == null ? 0 : hole.getViewCount()) + 1);
        return hole;
    }

    public Long create(Map<String, String> body) {
        String content = body.getOrDefault("content", "");
        if (!StringUtils.hasText(content) || content.trim().length() > 1000) {
            throw new BusinessException("树洞内容不能为空且不超过 1000 字");
        }
        Treehole hole = new Treehole();
        hole.setNickname(StringUtils.hasText(body.get("nickname")) ? body.get("nickname").trim() : "匿名");
        hole.setAvatar(StringUtils.hasText(body.get("avatar")) ? body.get("avatar").trim() : null);
        hole.setMood(StringUtils.hasText(body.get("mood")) ? body.get("mood").trim() : null);
        hole.setColor(StringUtils.hasText(body.get("color")) ? body.get("color").trim() : null);
        hole.setContent(content.trim());
        hole.setLikeCount(0);
        hole.setViewCount(0);
        hole.setCreatedAt(LocalDateTime.now());
        holeMapper.insert(hole);
        return hole.getId();
    }

    public int like(Long id) {
        Treehole hole = holeMapper.selectById(id);
        if (hole == null) {
            throw new BusinessException(404, "树洞不存在");
        }
        holeMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Treehole>()
                .eq(Treehole::getId, id)
                .setSql("like_count = like_count + 1"));
        return (hole.getLikeCount() == null ? 0 : hole.getLikeCount()) + 1;
    }

    public List<TreeholeComment> comments(Long holeId) {
        return commentMapper.selectList(new LambdaQueryWrapper<TreeholeComment>()
                .eq(TreeholeComment::getHoleId, holeId)
                .orderByAsc(TreeholeComment::getCreatedAt));
    }

    public Long comment(Long holeId, String nickname, String content) {
        if (holeMapper.selectById(holeId) == null) {
            throw new BusinessException(404, "树洞不存在");
        }
        if (!StringUtils.hasText(content) || content.trim().length() > 500) {
            throw new BusinessException("评论内容不能为空且不超过 500 字");
        }
        TreeholeComment c = new TreeholeComment();
        c.setHoleId(holeId);
        c.setNickname(StringUtils.hasText(nickname) ? nickname.trim() : "匿名");
        c.setContent(content.trim());
        c.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(c);
        return c.getId();
    }
}
