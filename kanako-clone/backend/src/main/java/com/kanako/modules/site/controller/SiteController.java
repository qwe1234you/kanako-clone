package com.kanako.modules.site.controller;

import com.kanako.common.jwt.RequireAdmin;
import com.kanako.common.response.R;
import com.kanako.modules.site.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api-common/site")
@RequiredArgsConstructor
public class SiteController {

    private final SiteConfigService configService;

    @GetMapping("/config")
    public R<Map<String, String>> config() {
        return R.ok(configService.getAll());
    }

    @PutMapping("/config")
    @RequireAdmin
    public R<Void> updateConfig(@RequestBody Map<String, String> configs) {
        configService.setAll(configs);
        return R.ok();
    }
}