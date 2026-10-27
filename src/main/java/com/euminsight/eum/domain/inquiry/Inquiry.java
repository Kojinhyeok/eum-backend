package com.euminsight.eum.domain.inquiry;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 홈페이지 상담 신청 (/contact)
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** GLOBAL / DOMESTIC / CLINICAL / LETTER / ETC */
    @Column(length = 20, nullable = false)
    private String inquiryType;

    @Column(length = 100, nullable = false)
    private String companyName;

    /** 담당자 성함 및 직함 */
    @Column(length = 100, nullable = false)
    private String contactName;

    @Column(length = 200, nullable = false)
    private String email;

    @Column(length = 50)
    private String phone;

    /** 제품군 및 목표 국가 */
    @Column(length = 200, nullable = false)
    private String productTarget;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** NEW(신규) / DONE(처리완료) */
    @Builder.Default
    @Column(length = 10, nullable = false)
    private String status = "NEW";

    /** 관리자 메모 */
    @Column(columnDefinition = "TEXT")
    private String adminMemo;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
