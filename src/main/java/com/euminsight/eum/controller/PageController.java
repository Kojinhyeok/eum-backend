package com.euminsight.eum.controller;

import com.euminsight.eum.domain.regulation.Region;
import com.euminsight.eum.domain.regulation.RegulationPost;
import com.euminsight.eum.domain.regulation.RegulationPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class PageController {

    /** 국가별 최신규정 한 페이지당 카드 수 */
    private static final int REGULATION_PAGE_SIZE = 6;

    private final RegulationPostRepository regulationPostRepository;

    @GetMapping("/")
    public String index(Model model) {
        List<RegulationPost> latest = regulationPostRepository.findTop3ByPublishedTrueOrderByNoticeDateDescIdDesc();
        model.addAttribute("latestPosts", latest);
        model.addAttribute("hasLatest", !latest.isEmpty());
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/global")
    public String global() {
        return "global";
    }

    @GetMapping("/domestic")
    public String domestic() {
        return "domestic";
    }

    @GetMapping("/clinical")
    public String clinical() {
        return "clinical";
    }

    @GetMapping("/regulations")
    public String regulations(@RequestParam(required = false) String region,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        Region current = Region.fromCode(region);
        Pageable pageable = PageRequest.of(Math.max(page, 0), REGULATION_PAGE_SIZE);

        Page<RegulationPost> result = (current == null)
                ? regulationPostRepository.findByPublishedTrueOrderByNoticeDateDescIdDesc(pageable)
                : regulationPostRepository.findByPublishedTrueAndRegionOrderByNoticeDateDescIdDesc(current, pageable);

        // 상단 요약 카드: 권역별 가장 최근 글 1개씩, 최신순 4개
        List<RegulationPost> summary = Arrays.stream(Region.values())
                .map(regulationPostRepository::findFirstByPublishedTrueAndRegionOrderByNoticeDateDescIdDesc)
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(RegulationPost::getNoticeDate).reversed())
                .limit(4)
                .toList();

        model.addAttribute("summaryPosts", summary);
        model.addAttribute("hasSummary", !summary.isEmpty());

        model.addAttribute("regions", Region.values());
        model.addAttribute("currentRegion", current);

        model.addAttribute("posts", result.getContent());
        model.addAttribute("hasPosts", result.hasContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("hasPrev", result.hasPrevious());
        model.addAttribute("hasNext", result.hasNext());
        model.addAttribute("pageNumbers", IntStream.range(0, result.getTotalPages()).boxed().toList());
        model.addAttribute("regionQuery", current == null ? "" : "&region=" + current.getCode());
        return "regulations";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/admin")
    public String adminRedirect() {
        return "redirect:/admin/index.html";
    }
}
