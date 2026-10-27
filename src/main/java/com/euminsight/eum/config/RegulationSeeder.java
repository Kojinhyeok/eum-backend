package com.euminsight.eum.config;

import com.euminsight.eum.domain.regulation.Region;
import com.euminsight.eum.domain.regulation.RegulationPost;
import com.euminsight.eum.domain.regulation.RegulationPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 국가별 최신규정 테이블이 비어 있을 때 1회만 초안(요청자 시안) 4건을 넣는다.
 * 관리자 페이지에서 수정·삭제하면 다시 생성되지 않는다(테이블이 비어있지 않으므로).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegulationSeeder implements CommandLineRunner {

    private final RegulationPostRepository regulationPostRepository;

    @Override
    public void run(String... args) {
        if (regulationPostRepository.count() > 0) return;

        List<RegulationPost> seeds = List.of(
                RegulationPost.builder()
                        .region(Region.US)
                        .category("최신 개정/고시")
                        .noticeDate(LocalDate.of(2025, 2, 1))
                        .effectiveNote("시행: 2024.12 ~ 상시")
                        .title("MoCRA 안전성 입증 및 시설/제품 리스팅 의무화 가이드")
                        .summary("처방별 안전성 입증(Safety Substantiation) 근거 기록 의무 시행 및 eSubmitter 포털을 통한 제조소·제품 리스팅 갱신 절차 확정.")
                        .impact("미국 수출 전 품목의 독성 평가 및 라벨 기재 연락처를 통한 유해사례(AER) 15일 이내 보고 체계가 필수 적용됩니다.")
                        .checkpoints("Dun & Bradstreet (DUNS) 번호 및 FEI 시설 식별번호 유효성 확인\n향료 알레르기 유발 성분 공개 표기 요건 대비 성분표 감수")
                        .keyPoint("전성분 라벨링 요건 및 시설 등록 갱신 주기 알림")
                        .published(true)
                        .build(),
                RegulationPost.builder()
                        .region(Region.EU)
                        .category("가이드라인 반영")
                        .noticeDate(LocalDate.of(2025, 2, 1))
                        .effectiveNote("적용: SCCS 12차 개정안")
                        .title("SCCS 안전성 평가 지침 개정 및 알러젠 확대 규제")
                        .summary("나노물질 안전성 마진(MoS) 산출 공식 업데이트 및 착향제 알레르기 유발물질 24종에서 56종 확대 라벨링 기준 대응.")
                        .impact("CPSR(화장품안전성보고서) Part A/B 재평가와 현지 RP의 라벨 표기 의무 업데이트가 필수적으로 수반됩니다.")
                        .checkpoints("공인 유러피언 안전성 평가사 서명 CPSR 최종본 갱신\nCPNP(EU) / SCPN(영국) 동시 등록 전산 데이터 일치 여부 확인")
                        .keyPoint("CPSR Part A/B 검토 기준 및 마이크로플라스틱 규제 대응")
                        .published(true)
                        .build(),
                RegulationPost.builder()
                        .region(Region.CN)
                        .category("긴급 고시 대응")
                        .noticeDate(LocalDate.of(2025, 1, 1))
                        .effectiveNote("전면 적용: 안전정보 플랫폼")
                        .title("기사용 원료 안전정보 코드 및 효능평가 실증 보고")
                        .summary("전성분 원료 안전성 보고 코드(안전정보 플랫폼 연계) 제출 필수화 및 인체시험·문헌 근거 효능 클레임 요약 보고서 등록.")
                        .impact("원료사 안전정보 코드 미확보 시 비안 접수 불가, 중국 경내책임인의 주기적 사후품질 관리 감사 요건이 강화되었습니다.")
                        .checkpoints("IECIC 목록 내 원료 최고 배합한도 준수 확인서 구비\nNMPA 인가 시험기관 미생물·이화학·독성 시험 보고서 준비")
                        .keyPoint("효능입증 평가자료 제출 요건 및 경내책임인 권한 관리 강화")
                        .published(true)
                        .build(),
                RegulationPost.builder()
                        .region(Region.ASEAN)
                        .category("규정 DB 동기화")
                        .noticeDate(LocalDate.of(2025, 2, 1))
                        .effectiveNote("인도네시아 BPOM / 할랄 의무")
                        .title("인니 할랄(BPJPH) 인증 의무화 및 베트남 신고서 개정")
                        .summary("인도네시아 수입 화장품 할랄 인증 연계 법안 단계별 시행 및 베트남 DAV 화장품 사전 통보 온라인 서식 개정.")
                        .impact("동남아 진출 시 ACD 기준 외에 국가별 특정 인허가(할랄 원료 추적, CFS 아포스티유 등) 사전 대비가 필수적입니다.")
                        .checkpoints("제조사 GMP 인증서 및 자유판매증명서(CFS) 대사관 공증\nACD Annex 금지·배합한도 원료 변경 리스트 대조 스크리닝")
                        .keyPoint("아세안 화장품 지침(ACD) 금지성분 Annex 개정 즉시 알림")
                        .published(true)
                        .build()
        );

        regulationPostRepository.saveAll(seeds);
        log.info(">>> 국가별 최신규정 초안 {}건 생성됨 (관리자 페이지에서 수정하세요)", seeds.size());
    }
}
