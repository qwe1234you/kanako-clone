package com.kanako.modules.friend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kanako.common.exception.BusinessException;
import com.kanako.modules.friend.entity.FriendCircle;
import com.kanako.modules.friend.entity.FriendLink;
import com.kanako.modules.friend.mapper.FriendCircleMapper;
import com.kanako.modules.friend.mapper.FriendLinkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendLinkMapper linkMapper;
    private final FriendCircleMapper circleMapper;

    public List<FriendLink> links() {
        return linkMapper.selectList(new LambdaQueryWrapper<FriendLink>()
                .eq(FriendLink::getStatus, "SHOW")
                .orderByAsc(FriendLink::getSort)
                .orderByAsc(FriendLink::getId));
    }

    public Page<FriendLink> adminPage(long page, long size, String status) {
        LambdaQueryWrapper<FriendLink> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            qw.eq(FriendLink::getStatus, status);
        }
        qw.orderByAsc(FriendLink::getStatus).orderByDesc(FriendLink::getCreatedAt);
        return linkMapper.selectPage(new Page<>(page, size), qw);
    }

    public Long apply(String name, String url, String description) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(url)) {
            throw new BusinessException("名称和 URL 不能为空");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new BusinessException("URL 需以 http(s):// 开头");
        }
        FriendLink link = new FriendLink();
        link.setName(name.trim());
        link.setUrl(url.trim());
        link.setDescription(StringUtils.hasText(description) ? description.trim() : null);
        link.setSort(99);
        link.setStatus("PENDING");
        link.setCreatedAt(LocalDateTime.now());
        linkMapper.insert(link);
        return link.getId();
    }

    public void updateStatus(Long id, String status) {
        FriendLink link = linkMapper.selectById(id);
        if (link == null) {
            throw new BusinessException(404, "友链不存在");
        }
        link.setStatus(status);
        linkMapper.updateById(link);
    }

    public void delete(Long id) {
        linkMapper.deleteById(id);
    }

    public List<Map<String, Object>> circleWithLink(long page, long size) {
        Page<FriendCircle> p = circleMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<FriendCircle>()
                        .orderByDesc(FriendCircle::getCreatedAt));
        Map<Long, FriendLink> linkMap = linkMapper.selectList(null).stream()
                .collect(Collectors.toMap(FriendLink::getId, l -> l));
        return p.getRecords().stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("content", c.getContent());
            m.put("images", c.getImages());
            m.put("createdAt", c.getCreatedAt());
            FriendLink link = c.getLinkId() == null ? null : linkMap.get(c.getLinkId());
            if (link != null) {
                m.put("linkName", link.getName());
                m.put("linkAvatar", link.getAvatar());
                m.put("linkUrl", link.getUrl());
            }
            return m;
        }).collect(Collectors.toList());
    }

    public Long createCircle(Long linkId, String content, String images) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("动态内容不能为空");
        }
        FriendCircle c = new FriendCircle();
        c.setLinkId(linkId);
        c.setContent(content.trim());
        c.setImages(StringUtils.hasText(images) ? images : null);
        c.setCreatedAt(LocalDateTime.now());
        circleMapper.insert(c);
        return c.getId();
    }

    public void deleteCircle(Long id) {
        circleMapper.deleteById(id);
    }
}
