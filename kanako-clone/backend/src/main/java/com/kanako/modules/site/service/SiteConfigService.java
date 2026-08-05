package com.kanako.modules.site.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanako.modules.site.entity.SiteConfig;
import com.kanako.modules.site.mapper.SiteConfigMapper;
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
public class SiteConfigService {

    private final SiteConfigMapper configMapper;

    public Map<String, String> getAll() {
        List<SiteConfig> list = configMapper.selectList(null);
        Map<String, String> map = new HashMap<>();
        for (SiteConfig c : list) {
            map.put(c.getConfigKey(), c.getConfigValue());
        }
        return map;
    }

    public String get(String key, String def) {
        SiteConfig c = configMapper.selectOne(new LambdaQueryWrapper<SiteConfig>()
                .eq(SiteConfig::getConfigKey, key).last("limit 1"));
        return c == null ? def : c.getConfigValue();
    }

    public void setAll(Map<String, String> configs) {
        for (Map.Entry<String, String> e : configs.entrySet()) {
            set(e.getKey(), e.getValue());
        }
    }

    public void set(String key, String value) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        SiteConfig c = configMapper.selectOne(new LambdaQueryWrapper<SiteConfig>()
                .eq(SiteConfig::getConfigKey, key).last("limit 1"));
        if (c == null) {
            c = new SiteConfig();
            c.setConfigKey(key);
            c.setConfigValue(value);
            c.setUpdatedAt(LocalDateTime.now());
            configMapper.insert(c);
        } else {
            c.setConfigValue(value);
            c.setUpdatedAt(LocalDateTime.now());
            configMapper.updateById(c);
        }
    }
}