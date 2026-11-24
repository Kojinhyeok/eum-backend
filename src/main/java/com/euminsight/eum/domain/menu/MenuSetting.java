package com.euminsight.eum.domain.menu;

import jakarta.persistence.*;
import lombok.*;

/**
 * 메뉴·게시판·국가 탭의 관리자 수정값 (이름 한/영, 표시 여부, 순서).
 * 메뉴 구조(상위 메뉴, 주소)는 {@link MenuCatalog}에 코드로 고정되어 있고,
 * 이 테이블에는 관리자가 바꿀 수 있는 값만 저장한다.
 */
@Entity
@Table(name = "menu_setting")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MenuSetting {

    /** MenuCatalog 코드 (ABOUT, BOARD_NOTICE, REGION_US ...) */
    @Id
    @Column(length = 40)
    private String code;

    @Column(nullable = false, length = 50)
    private String labelKo;

    /** 비워두면 EN 화면에서 구글 번역 사용 */
    @Column(length = 80)
    private String labelEn;

    @Column(nullable = false)
    private boolean visible;

    @Column(nullable = false)
    private int sortOrder;
}