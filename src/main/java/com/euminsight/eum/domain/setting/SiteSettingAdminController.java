package com.euminsight.eum.domain.setting;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 관리자: 사이트 설정 (회사소개 대표 사진 등)
 */
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class SiteSettingAdminController {

    private final SiteSettingService siteSettingService;

    @GetMapping
    public Map<String, String> get() {
        return siteSettingService.editableValues();
    }

    @PutMapping
    public Map<String, Object> save(@RequestBody Map<String, String> values) {
        siteSettingService.update(values);
        return Map.of("ok", true);
    }
}