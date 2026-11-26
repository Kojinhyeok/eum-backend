package com.euminsight.eum.domain.setting;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 사이트 설정값 조회·저장 (메모리 캐시, 저장 시 비움)
 */
@Service
@RequiredArgsConstructor
public class SiteSettingService {

    /** 회사소개 대표 사진 */
    public static final String CEO_PHOTO_URL = "about.ceoPhotoUrl";

    /** 관리자에서 바꿀 수 있는 키 (그 외 키는 저장 거부) */
    private static final Set<String> EDITABLE_KEYS = Set.of(CEO_PHOTO_URL);

    private final SiteSettingRepository siteSettingRepository;

    private volatile Map<String, String> cache;

    public String get(String key) {
        String v = all().get(key);
        return v == null || v.isBlank() ? null : v;
    }

    public Map<String, String> editableValues() {
        Map<String, String> result = new LinkedHashMap<>();
        for (String k : EDITABLE_KEYS) result.put(k, get(k) == null ? "" : get(k));
        return result;
    }

    @Transactional
    public void update(Map<String, String> values) {
        if (values == null) return;
        for (Map.Entry<String, String> e : values.entrySet()) {
            String key = e.getKey();
            if (!EDITABLE_KEYS.contains(key)) throw new IllegalArgumentException("알 수 없는 설정입니다: " + key);
            String value = e.getValue() == null ? "" : e.getValue().trim();
            validate(key, value);
            SiteSetting s = siteSettingRepository.findById(key).orElseGet(() -> SiteSetting.builder().key(key).build());
            s.setValue(value);
            siteSettingRepository.save(s);
        }
        cache = null;
    }

    private void validate(String key, String value) {
        if (CEO_PHOTO_URL.equals(key) && !value.isEmpty() && !value.matches("(?i)^https?://.+")) {
            throw new IllegalArgumentException("대표 사진 주소가 올바르지 않습니다.");
        }
    }

    private Map<String, String> all() {
        Map<String, String> c = cache;
        if (c == null) {
            c = siteSettingRepository.findAll().stream()
                    .filter(s -> s.getValue() != null)
                    .collect(Collectors.toUnmodifiableMap(SiteSetting::getKey, SiteSetting::getValue, (a, b) -> a));
            cache = c;
        }
        return c;
    }
}