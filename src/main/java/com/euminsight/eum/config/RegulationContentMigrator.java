package com.euminsight.eum.config;

import com.euminsight.eum.domain.regulation.RegulationPost;
import com.euminsight.eum.domain.regulation.RegulationPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 글로벌 규제동향 개편(2026-11): 예전 칸으로 나뉜 글을 본문(content) 하나로 옮긴다.
 * - 본문이 비어 있는 글만 대상 → 관리자가 쓴 본문은 절대 덮어쓰지 않음
 * - 예전 칸(summary / keyPoint / impact / checkpoints) 값은 지우지 않음 (되돌리기용)
 */
@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class RegulationContentMigrator implements CommandLineRunner {

    private final RegulationPostRepository regulationPostRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<RegulationPost> targets = regulationPostRepository.findAll().stream()
                .filter(p -> !p.isHasContent())
                .toList();
        if (targets.isEmpty()) return;

        for (RegulationPost p : targets) {
            p.setContent(buildHtml(p));
        }
        regulationPostRepository.saveAll(targets);
        log.info(">>> 글로벌 규제동향 {}건을 새 본문 형식으로 옮겼습니다.", targets.size());
    }

    private String buildHtml(RegulationPost p) {
        StringBuilder sb = new StringBuilder();
        section(sb, "개정 내용", p.getSummary());
        section(sb, "규제 변경 포인트", p.getKeyPoint());
        section(sb, "규제 영향", p.getImpact());
        List<String> checks = p.getCheckpointList();
        if (!checks.isEmpty()) {
            sb.append("<h3>체크포인트</h3><ul>");
            for (String c : checks) sb.append("<li>").append(HtmlUtils.htmlEscape(c)).append("</li>");
            sb.append("</ul>");
        }
        return sb.length() == 0 ? "<p></p>" : sb.toString();
    }

    private void section(StringBuilder sb, String title, String text) {
        if (text == null || text.isBlank()) return;
        String body = HtmlUtils.htmlEscape(text.trim()).replace("\r\n", "\n").replace("\n", "<br>");
        sb.append("<h3>").append(title).append("</h3><p>").append(body).append("</p>");
    }
}