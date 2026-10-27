package com.euminsight.eum.domain.regulation;

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

    public record RegulationRequest(
            String region, String category, String noticeDate, String effectiveNote,
            String title, String summary, String impact, String checkpoints,
            String keyPoint, String sourceUrl, Boolean published
    ) {}

    /** 관리자 화면 select 박스용 권역 목록 */
    @GetMapping("/regions")
    public List<Map<String, String>> regions() {
        return Arrays.stream(Region.values())
                .map(r -> Map.of("code", r.getCode(), "label", r.getLabel(), "shortCode", r.getShortCode()))
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
        String summary = trim(req.summary());
        if (title == null) throw new IllegalArgumentException("제목을 입력해 주세요.");
        if (summary == null) throw new IllegalArgumentException("개정 내용 요약을 입력해 주세요.");
        if (title.length() > 200) throw new IllegalArgumentException("제목은 200자 이내로 입력해 주세요.");

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
        post.setImpact(trim(req.impact()));
        post.setCheckpoints(trim(req.checkpoints()));
        post.setKeyPoint(trim(req.keyPoint()));
        post.setSourceUrl(sourceUrl);
        post.setPublished(req.published() == null || req.published());
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
