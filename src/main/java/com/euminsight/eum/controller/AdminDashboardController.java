package com.euminsight.eum.controller;

import com.euminsight.eum.domain.inquiry.InquiryRepository;
import com.euminsight.eum.domain.regulation.RegulationPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AdminDashboardController {

    private final InquiryRepository inquiryRepository;
    private final RegulationPostRepository regulationPostRepository;

    @GetMapping("/api/admin/dashboard")
    public Map<String, Long> dashboard() {
        return Map.of(
                "newInquiries", inquiryRepository.countByStatus("NEW"),
                "totalInquiries", inquiryRepository.count(),
                "publishedRegulations", regulationPostRepository.countByPublishedTrue(),
                "totalRegulations", regulationPostRepository.count()
        );
    }
}
