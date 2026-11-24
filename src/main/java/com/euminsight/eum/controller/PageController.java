package com.euminsight.eum.controller;

import com.euminsight.eum.domain.menu.MenuService;
import com.euminsight.eum.domain.regulation.Region;
import com.euminsight.eum.domain.regulation.RegulationPost;
import com.euminsight.eum.domain.regulation.RegulationPostRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class PageController {

    /** 글로벌 규제동향 한 페이지당 글 수 */
    private static final int REGULATION_PAGE_SIZE = 10;

    private final RegulationPostRepository regulationPostRepository;
    private final MenuService menuService;

    @GetMapping("/")
    public String index(Model model) {
        List<Region> visible = visibleRegions();
        List<RegulationPost> latest = visible.isEmpty() ? List.of()
                : regulationPostRepository.findTop3ByPublishedTrueAndRegionInOrderByNoticeDateDescIdDesc(visible);
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
        if (!menuService.isRegulationsVisible()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        // 관리자 > 메뉴·탭 관리에서 숨긴 국가는 탭·목록에서 모두 제외
        List<Region> visible = visibleRegions();
        Region current = Region.fromCode(region);
        if (current != null && !visible.contains(current)) current = null;
        Pageable pageable = PageRequest.of(Math.max(page, 0), REGULATION_PAGE_SIZE);

        Page<RegulationPost> result;
        if (current != null) {
            result = regulationPostRepository.findByPublishedTrueAndRegionOrderByNoticeDateDescIdDesc(current, pageable);
        } else if (visible.isEmpty()) {
            result = Page.empty(pageable);
        } else {
            result = regulationPostRepository.findByPublishedTrueAndRegionInOrderByNoticeDateDescIdDesc(visible, pageable);
        }

        model.addAttribute("pageTitle", menuService.labelKo("BOARD_REGULATIONS"));
        model.addAttribute("pageTitleEn", menuService.labelEn("BOARD_REGULATIONS"));
        model.addAttribute("regions", menuService.visibleRegions());
        model.addAttribute("currentRegion", current);

        // 국가 이름(한/영)은 미리 계산해서 넘김 (Thymeleaf 3.1은 th:attr 안에서 빈 메서드 호출을 막음)
        model.addAttribute("rows", result.getContent().stream()
                .map(p -> new RegulationRow(p, menuService.regionLabel(p.getRegion()), menuService.regionLabelEn(p.getRegion())))
                .toList());
        model.addAttribute("hasPosts", result.hasContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("hasPrev", result.hasPrevious());
        model.addAttribute("hasNext", result.hasNext());
        model.addAttribute("pageNumbers", IntStream.range(0, result.getTotalPages()).boxed().toList());
        model.addAttribute("regionQuery", current == null ? "" : "&region=" + current.getCode());
        return "regulations";
    }

    @GetMapping("/regulations/{id}")
    public String regulationDetail(@PathVariable Long id, Model model) {
        if (!menuService.isRegulationsVisible()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        RegulationPost post = regulationPostRepository.findById(id)
                .filter(RegulationPost::isPublished)
                .filter(p -> menuService.isRegionVisible(p.getRegion()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("pageTitle", menuService.labelKo("BOARD_REGULATIONS"));
        model.addAttribute("pageTitleEn", menuService.labelEn("BOARD_REGULATIONS"));
        model.addAttribute("post", post);
        model.addAttribute("regionLabel", menuService.regionLabel(post.getRegion()));
        model.addAttribute("regionLabelEn", menuService.regionLabelEn(post.getRegion()));
        return "regulation-detail";
    }

    /** 글로벌 규제동향 목록 한 줄 (글 + 국가 이름 한/영) */
    @Getter
    @AllArgsConstructor
    public static class RegulationRow {
        private final RegulationPost post;
        private final String regionLabel;
        private final String regionLabelEn;
    }

    private List<Region> visibleRegions() {
        return menuService.visibleRegions().stream().map(MenuService.RegionItem::getRegion).toList();
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