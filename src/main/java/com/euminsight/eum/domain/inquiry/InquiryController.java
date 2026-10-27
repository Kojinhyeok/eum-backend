package com.euminsight.eum.domain.inquiry;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private static final Set<String> TYPES = Set.of("GLOBAL", "DOMESTIC", "CLINICAL", "LETTER", "ETC");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final InquiryRepository inquiryRepository;

    public record InquiryRequest(
            String inquiryType, String companyName, String contactName, String email, String phone,
            String productTarget, String content, Boolean agreed,
            String website // 스팸 방지용 숨김 필드 (사람은 비워서 보냄)
    ) {}

    @PostMapping
    public Map<String, Object> create(@RequestBody InquiryRequest req) {
        // 봇이 숨김 필드를 채운 경우: 저장하지 않고 정상 응답만 돌려준다
        if (req.website() != null && !req.website().isBlank()) {
            return Map.of("ok", true);
        }

        if (!Boolean.TRUE.equals(req.agreed())) {
            throw new IllegalArgumentException("개인정보 수집·이용 및 기밀유지 기준에 동의해 주세요.");
        }

        String type = TYPES.contains(req.inquiryType()) ? req.inquiryType() : "ETC";
        String company = required(req.companyName(), "회사명", 100);
        String contact = required(req.contactName(), "담당자 성함 및 직함", 100);
        String email = required(req.email(), "업무용 이메일", 200);
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
        String productTarget = required(req.productTarget(), "제품군 및 목표 국가", 200);
        String content = required(req.content(), "상세 문의 내용", 5000);
        String phone = optional(req.phone(), "연락처", 50);

        Inquiry inquiry = Inquiry.builder()
                .inquiryType(type)
                .companyName(company)
                .contactName(contact)
                .email(email)
                .phone(phone)
                .productTarget(productTarget)
                .content(content)
                .build();
        inquiryRepository.save(inquiry);

        return Map.of("ok", true, "id", inquiry.getId());
    }

    private static String required(String value, String label, int max) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty()) throw new IllegalArgumentException(label + "을(를) 입력해 주세요.");
        if (v.length() > max) throw new IllegalArgumentException(label + "은(는) " + max + "자 이내로 입력해 주세요.");
        return v;
    }

    private static String optional(String value, String label, int max) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        if (v.length() > max) throw new IllegalArgumentException(label + "은(는) " + max + "자 이내로 입력해 주세요.");
        return v;
    }
}
