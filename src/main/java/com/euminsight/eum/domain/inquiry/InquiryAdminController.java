package com.euminsight.eum.domain.inquiry;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin/inquiry")
@RequiredArgsConstructor
public class InquiryAdminController {

    private final InquiryRepository inquiryRepository;

    public record StatusRequest(String status, String adminMemo) {}

    @GetMapping
    public List<Inquiry> list() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc();
    }

    /** 처리 상태(NEW/DONE)와 관리자 메모 변경 */
    @PatchMapping("/{id}")
    @Transactional
    public Map<String, Object> update(@PathVariable Long id, @RequestBody StatusRequest req) {
        Inquiry inquiry = inquiryRepository.findById(id).orElseThrow(NoSuchElementException::new);
        if (req.status() != null) {
            if (!"NEW".equals(req.status()) && !"DONE".equals(req.status())) {
                throw new IllegalArgumentException("상태 값이 올바르지 않습니다.");
            }
            inquiry.setStatus(req.status());
        }
        if (req.adminMemo() != null) {
            inquiry.setAdminMemo(req.adminMemo().isBlank() ? null : req.adminMemo().trim());
        }
        inquiryRepository.save(inquiry);
        return Map.of("id", inquiry.getId(), "status", inquiry.getStatus());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        inquiryRepository.deleteById(id);
    }
}
