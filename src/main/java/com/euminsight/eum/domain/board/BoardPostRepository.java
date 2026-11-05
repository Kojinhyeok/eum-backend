package com.euminsight.eum.domain.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    // 공개 화면: 고정글 먼저, 그다음 최신순
    Page<BoardPost> findByBoardAndPublishedTrueOrderByPinnedDescCreatedAtDesc(BoardType board, Pageable pageable);

    Optional<BoardPost> findByIdAndBoardAndPublishedTrue(Long id, BoardType board);

    // 관리자
    List<BoardPost> findAllByOrderByCreatedAtDesc();

    long countByPublishedTrue();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update BoardPost p set p.viewCount = p.viewCount + 1 where p.id = :id")
    int increaseViewCount(@Param("id") Long id);
}