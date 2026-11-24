package com.euminsight.eum.domain.regulation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RegulationPostRepository extends JpaRepository<RegulationPost, Long> {

    // 공개 화면
    Page<RegulationPost> findByPublishedTrueOrderByNoticeDateDescIdDesc(Pageable pageable);

    Page<RegulationPost> findByPublishedTrueAndRegionOrderByNoticeDateDescIdDesc(Region region, Pageable pageable);

    Optional<RegulationPost> findFirstByPublishedTrueAndRegionOrderByNoticeDateDescIdDesc(Region region);

    List<RegulationPost> findTop3ByPublishedTrueOrderByNoticeDateDescIdDesc();

    // 공개 화면: 관리자 > 메뉴·탭 관리에서 표시 중인 국가만
    Page<RegulationPost> findByPublishedTrueAndRegionInOrderByNoticeDateDescIdDesc(Collection<Region> regions, Pageable pageable);

    List<RegulationPost> findTop3ByPublishedTrueAndRegionInOrderByNoticeDateDescIdDesc(Collection<Region> regions);

    // 관리자
    List<RegulationPost> findAllByOrderByNoticeDateDescIdDesc();

    long countByPublishedTrue();
}