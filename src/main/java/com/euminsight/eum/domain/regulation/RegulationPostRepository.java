package com.euminsight.eum.domain.regulation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegulationPostRepository extends JpaRepository<RegulationPost, Long> {

    // 공개 화면
    Page<RegulationPost> findByPublishedTrueOrderByNoticeDateDescIdDesc(Pageable pageable);

    Page<RegulationPost> findByPublishedTrueAndRegionOrderByNoticeDateDescIdDesc(Region region, Pageable pageable);

    Optional<RegulationPost> findFirstByPublishedTrueAndRegionOrderByNoticeDateDescIdDesc(Region region);

    List<RegulationPost> findTop3ByPublishedTrueOrderByNoticeDateDescIdDesc();

    // 관리자
    List<RegulationPost> findAllByOrderByNoticeDateDescIdDesc();

    long countByPublishedTrue();
}
