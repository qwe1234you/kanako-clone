package com.kanako.modules.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kanako.common.exception.BusinessException;
import com.kanako.modules.blog.dto.BlogDTO;
import com.kanako.modules.blog.entity.*;
import com.kanako.modules.blog.mapper.*;
import com.kanako.modules.blog.vo.BlogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogMapper blogMapper;
    private final BlogCategoryMapper categoryMapper;
    private final BlogTagMapper tagMapper;
    private final BlogPostTagMapper postTagMapper;

    /**
     * 公开分页查询
     */
    public Page<BlogVO> pagePublic(long page, long size, String categorySlug, String tagSlug, String keyword) {
        LambdaQueryWrapper<Blog> qw = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getStatus, "PUBLISHED");
        applyFilters(qw, categorySlug, tagSlug, keyword);
        qw.orderByDesc(Blog::getIsTop)
                .orderByDesc(Blog::getCreatedAt);
        return toVOPage(blogMapper.selectPage(new Page<>(page, size), qw), false);
    }

    /**
     * 管理端分页查询（含草稿）
     */
    public Page<BlogVO> pageAdmin(long page, long size, String status) {
        LambdaQueryWrapper<Blog> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            qw.eq(Blog::getStatus, status);
        }
        qw.orderByDesc(Blog::getCreatedAt);
        return toVOPage(blogMapper.selectPage(new Page<>(page, size), qw), false);
    }

    public BlogVO detail(String slugOrId) {
        Blog blog = findOne(slugOrId);
        if (blog == null || "DRAFT".equals(blog.getStatus())) {
            throw new BusinessException(404, "文章不存在");
        }
        blogMapper.update(null, new LambdaUpdateWrapper<Blog>()
                .eq(Blog::getId, blog.getId())
                .setSql("view_count = view_count + 1"));
        blog.setViewCount(blog.getViewCount() + 1);
        return toVO(blog, true);
    }

    public BlogVO adminDetail(Long id) {
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            throw new BusinessException(404, "文章不存在");
        }
        return toVO(blog, true);
    }

    private Blog findOne(String slugOrId) {
        Blog blog = null;
        if (slugOrId.matches("\\d+")) {
            blog = blogMapper.selectById(Long.parseLong(slugOrId));
        }
        if (blog == null) {
            blog = blogMapper.selectOne(new LambdaQueryWrapper<Blog>()
                    .eq(Blog::getSlug, slugOrId)
                    .last("limit 1"));
        }
        return blog;
    }

    @Transactional
    public Long create(BlogDTO dto) {
        Blog blog = new Blog();
        applyDto(blog, dto);
        blog.setViewCount(0L);
        blog.setLikeCount(0);
        blog.setStatus("DRAFT".equals(dto.getStatus()) ? "DRAFT" : "PUBLISHED");
        blog.setCreatedAt(LocalDateTime.now());
        blog.setUpdatedAt(LocalDateTime.now());
        blogMapper.insert(blog);
        saveTags(blog.getId(), dto.getTagIds());
        return blog.getId();
    }

    @Transactional
    public void update(Long id, BlogDTO dto) {
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            throw new BusinessException(404, "文章不存在");
        }
        blog.setTitle(dto.getTitle());
        blog.setSummary(dto.getSummary());
        blog.setCover(dto.getCover());
        blog.setContentMd(dto.getContentMd());
        blog.setCategoryId(dto.getCategoryId());
        blog.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : blog.getStatus());
        blog.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        blog.setUpdatedAt(LocalDateTime.now());
        ensureSlug(blog, dto.getSlug());
        blogMapper.updateById(blog);
        saveTags(blog.getId(), dto.getTagIds());
    }

    @Transactional
    public void delete(Long id) {
        blogMapper.deleteById(id);
        postTagMapper.delete(new LambdaQueryWrapper<BlogPostTag>().eq(BlogPostTag::getBlogId, id));
    }

    public long countByCategory(Long categoryId) {
        return blogMapper.selectCount(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getCategoryId, categoryId)
                .eq(Blog::getStatus, "PUBLISHED"));
    }

    public long countByTag(Long tagId) {
        return blogMapper.selectCount(new LambdaQueryWrapper<Blog>()
                .inSql(Blog::getId, "SELECT blog_id FROM blog_post_tag WHERE tag_id = " + tagId)
                .eq(Blog::getStatus, "PUBLISHED"));
    }

    private void applyDto(Blog blog, BlogDTO dto) {
        blog.setTitle(dto.getTitle());
        blog.setSummary(dto.getSummary());
        blog.setCover(dto.getCover());
        blog.setContentMd(dto.getContentMd());
        blog.setCategoryId(dto.getCategoryId());
        blog.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        ensureSlug(blog, dto.getSlug());
    }

    private void ensureSlug(Blog blog, String slug) {
        String base = StringUtils.hasText(slug) ? slug : blog.getTitle();
        if (!StringUtils.hasText(base)) {
            throw new BusinessException("缺少标题或 slug");
        }
        String candidate = base;
        int suffix = 2;
        while (true) {
            Long exist = blogMapper.selectCount(new LambdaQueryWrapper<Blog>()
                    .eq(Blog::getSlug, candidate)
                    .ne(blog.getId() != null, Blog::getId, blog.getId()));
            if (exist == null || exist == 0) {
                break;
            }
            candidate = base + "-" + suffix++;
        }
        blog.setSlug(candidate);
    }

    private void saveTags(Long blogId, List<Long> tagIds) {
        postTagMapper.delete(new LambdaQueryWrapper<BlogPostTag>().eq(BlogPostTag::getBlogId, blogId));
        if (tagIds == null) {
            return;
        }
        for (Long tid : tagIds) {
            BlogPostTag pt = new BlogPostTag();
            pt.setBlogId(blogId);
            pt.setTagId(tid);
            postTagMapper.insert(pt);
        }
    }

    private void applyFilters(LambdaQueryWrapper<Blog> qw, String categorySlug, String tagSlug, String keyword) {
        if (StringUtils.hasText(categorySlug)) {
            BlogCategory cat = categoryMapper.selectOne(new LambdaQueryWrapper<BlogCategory>()
                    .eq(BlogCategory::getSlug, categorySlug).last("limit 1"));
            if (cat != null) {
                qw.eq(Blog::getCategoryId, cat.getId());
            }
        }
        if (StringUtils.hasText(tagSlug)) {
            BlogTag tag = tagMapper.selectOne(new LambdaQueryWrapper<BlogTag>()
                    .eq(BlogTag::getSlug, tagSlug).last("limit 1"));
            if (tag != null) {
                qw.inSql(Blog::getId, "SELECT blog_id FROM blog_post_tag WHERE tag_id = " + tag.getId());
            }
        }
        if (StringUtils.hasText(keyword)) {
            qw.like(Blog::getTitle, keyword);
        }
    }

    private Page<BlogVO> toVOPage(Page<Blog> page, boolean withContent) {
        Page<BlogVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(toVOList(page.getRecords(), withContent));
        return voPage;
    }

    private List<BlogVO> toVOList(List<Blog> blogs, boolean withContent) {
        if (blogs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> categoryIds = blogs.stream().map(Blog::getCategoryId)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, BlogCategory> catMap = categoryIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(BlogCategory::getId, Function.identity()));

        List<Long> blogIds = blogs.stream().map(Blog::getId).toList();
        List<BlogPostTag> pts = postTagMapper.selectList(
                new LambdaQueryWrapper<BlogPostTag>().in(BlogPostTag::getBlogId, blogIds));
        List<Long> tagIds = pts.stream().map(BlogPostTag::getTagId).distinct().toList();
        Map<Long, BlogTag> tagMap = tagIds.isEmpty() ? Collections.emptyMap()
                : tagMapper.selectBatchIds(tagIds).stream()
                        .collect(Collectors.toMap(BlogTag::getId, Function.identity()));
        Map<Long, List<BlogTag>> tagsByBlog = pts.stream()
                .filter(p -> tagMap.containsKey(p.getTagId()))
                .collect(Collectors.groupingBy(BlogPostTag::getBlogId,
                        Collectors.mapping(p -> tagMap.get(p.getTagId()), Collectors.toList())));

        List<BlogVO> vos = new ArrayList<>(blogs.size());
        for (Blog b : blogs) {
            BlogVO vo = new BlogVO();
            vo.setId(b.getId());
            vo.setTitle(b.getTitle());
            vo.setSlug(b.getSlug());
            vo.setSummary(b.getSummary());
            vo.setCover(b.getCover());
            vo.setCategoryId(b.getCategoryId());
            vo.setCategory(catMap.get(b.getCategoryId()));
            vo.setTags(tagsByBlog.getOrDefault(b.getId(), new ArrayList<>()));
            vo.setStatus(b.getStatus());
            vo.setViewCount(b.getViewCount());
            vo.setLikeCount(b.getLikeCount());
            vo.setIsTop(b.getIsTop());
            vo.setCreatedAt(b.getCreatedAt());
            vo.setUpdatedAt(b.getUpdatedAt());
            if (withContent) {
                vo.setContentMd(b.getContentMd());
            }
            vos.add(vo);
        }
        return vos;
    }

    private BlogVO toVO(Blog blog, boolean withContent) {
        return toVOList(List.of(blog), withContent).get(0);
    }

    // ---------- 分类 / 标签 ----------

    public List<BlogCategory> categories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<BlogCategory>().orderByAsc(BlogCategory::getSort));
    }

    public List<BlogTag> tags() {
        return tagMapper.selectList(new LambdaQueryWrapper<BlogTag>().orderByDesc(BlogTag::getCreatedAt));
    }

    /**
     * 带文章数的分类列表
     */
    public List<Map<String, Object>> categoriesWithCount() {
        List<BlogCategory> list = categories();
        return list.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("tag", c);
            m.put("count", countByCategory(c.getId()));
            return m;
        }).toList();
    }

    /**
     * 带文章数的标签列表
     */
    public List<Map<String, Object>> tagsWithCount() {
        List<BlogTag> list = tags();
        return list.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("tag", t);
            m.put("count", countByTag(t.getId()));
            return m;
        }).toList();
    }

    /**
     * 归档：分类 + 标签 + 按年分组文章
     */
    public Map<String, Object> archive() {
        List<Blog> blogs = blogMapper.selectList(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getStatus, "PUBLISHED")
                .orderByDesc(Blog::getCreatedAt));
        List<BlogVO> vos = toVOList(blogs, false);
        Map<String, List<BlogVO>> byYear = vos.stream().collect(Collectors.groupingBy(
                b -> String.valueOf(b.getCreatedAt() != null ? b.getCreatedAt().getYear() : 2026),
                Collectors.toList()));
        Map<String, Object> result = new HashMap<>();
        result.put("categories", categoriesWithCount());
        result.put("tags", tagsWithCount());
        result.put("archives", byYear);
        return result;
    }

    public void createCategory(Long id, String name, String slug, String description, Integer sort) {
        if (id == null) {
            BlogCategory c = new BlogCategory();
            c.setName(name);
            c.setSlug(slug);
            c.setDescription(description);
            c.setSort(sort != null ? sort : 0);
            c.setCreatedAt(LocalDateTime.now());
            categoryMapper.insert(c);
            return;
        }
        BlogCategory c = categoryMapper.selectById(id);
        if (c == null) {
            throw new BusinessException(404, "分类不存在");
        }
        c.setName(name);
        c.setSlug(slug);
        c.setDescription(description);
        c.setSort(sort != null ? sort : 0);
        categoryMapper.updateById(c);
    }

    public void deleteCategory(Long id) {
        long n = countByCategory(id);
        if (n > 0) {
            throw new BusinessException("该分类下还有 " + n + " 篇文章，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    public void updateTag(Long id, String name, String slug) {
        BlogTag t = tagMapper.selectById(id);
        if (t == null) {
            throw new BusinessException(404, "标签不存在");
        }
        t.setName(name);
        t.setSlug(slug);
        tagMapper.updateById(t);
    }

    public void createTag(String name, String slug) {
        BlogTag t = new BlogTag();
        t.setName(name);
        t.setSlug(slug);
        t.setCreatedAt(LocalDateTime.now());
        tagMapper.insert(t);
    }

    public void deleteTag(Long id) {
        postTagMapper.delete(new LambdaQueryWrapper<BlogPostTag>().eq(BlogPostTag::getTagId, id));
        tagMapper.deleteById(id);
    }
}