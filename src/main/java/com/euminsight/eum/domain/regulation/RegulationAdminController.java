package com.euminsight.eum.domain.regulation;

import com.euminsight.eum.common.HtmlSanitizer;
import com.euminsight.eum.domain.menu.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin/regulations")
@RequiredArgsConstructor
public class RegulationAdminController {

    private final RegulationPostRepository regulationPostRepository;
    private final MenuService menuService;

    public record RegulationRequest(
            String region, String category, String noticeDate, String effectiveNote,
            String title, String summary, String content, String thumbnailUrl,
            String sourceUrl, Boolean published
    ) {}

    /** 관리자 화면 select 박스용 권역 목록 */
    @GetMapping("/regions")
    public List<Map<String, String>> regions() {
        return Arrays.stream(Region.values())
                .map(r -> Map.of("code", r.getCode(), "label", menuService.regionLabel(r), "shortCode", r.getShortCode(),
                        "visible", String.valueOf(menuService.isRegionVisible(r))))
                .toList();
    }

    @GetMapping
    public List<RegulationPost> list() {
        return regulationPostRepository.findAllByOrderByNoticeDateDescIdDesc();
    }

    @GetMapping("/{id}")
    public RegulationPost get(@PathVariable Long id) {
        return regulationPostRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody RegulationRequest req) {
        RegulationPost post = new RegulationPost();
        apply(post, req);
        regulationPostRepository.save(post);
        return Map.of("id", post.getId());
    }

    @PutMapping("/{id}")
    @Transactional
    public Map<String, Object> update(@PathVariable Long id, @RequestBody RegulationRequest req) {
        RegulationPost post = regulationPostRepository.findById(id).orElseThrow(NoSuchElementException::new);
        apply(post, req);
        regulationPostRepository.save(post);
        return Map.of("id", post.getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        regulationPostRepository.deleteById(id);
    }

    private void apply(RegulationPost post, RegulationRequest req) {
        Region region = Region.fromCode(req.region());
        if (region == null) throw new IllegalArgumentException("권역을 선택해 주세요.");

        String title = trim(req.title());
        if (title == null) throw new IllegalArgumentException("제목을 입력해 주세요.");
        if (title.length() > 200) throw new IllegalArgumentException("제목은 200자 이내로 입력해 주세요.");

        String content = HtmlSanitizer.cleanRich(req.content());
        if (HtmlSanitizer.isBlank(content)) throw new IllegalArgumentException("본문을 입력해 주세요.");

        // 목록 요약: 비우면 본문 앞부분으로 자동 작성
        String summary = trim(req.summary());
        if (summary == null) {
            String text = HtmlSanitizer.text(content).replaceAll("\\s+", " ").trim();
            summary = text.length() > 160 ? text.substring(0, 160) + "…" : text;
            if (summary.isEmpty()) summary = title;
        }
        if (summary.length() > 500) throw new IllegalArgumentException("목록 요약은 500자 이내로 입력해 주세요.");

        String thumbnailUrl = trim(req.thumbnailUrl());
        if (thumbnailUrl != null && !thumbnailUrl.matches("(?i)^https?://.+")) {
            throw new IllegalArgumentException("대표 이미지 주소가 올바르지 않습니다.");
        }

        LocalDate date;
        try {
            date = req.noticeDate() == null || req.noticeDate().isBlank()
                    ? LocalDate.now()
                    : LocalDate.parse(req.noticeDate().trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("업데이트 날짜 형식이 올바르지 않습니다. (예: 2026-10-27)");
        }

        String sourceUrl = trim(req.sourceUrl());
        if (sourceUrl != null && !sourceUrl.matches("(?i)^https?://.+")) {
            throw new IllegalArgumentException("원문 링크는 http:// 또는 https:// 로 시작해야 합니다.");
        }

        String category = trim(req.category());

        post.setRegion(region);
        post.setCategory(category == null ? "최신 개정/고시" : category);
        post.setNoticeDate(date);
        post.setEffectiveNote(trim(req.effectiveNote()));
        post.setTitle(title);
        post.setSummary(summary);
        post.setContent(content);
        post.setThumbnailUrl(thumbnailUrl);
        // impact / checkpoints / keyPoint 는 예전 칸 → 건드리지 않음 (되돌리기용 보존)
        post.setSourceUrl(sourceUrl);
        post.setPublished(req.published() == null || req.published());
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}