package com.euminsight.eum.domain.setting;

import jakarta.persistence.*;
import lombok.*;

/**
 * 사이트 설정값 (키-값). 관리자 > 사이트 설정에서 바꾸는 값을 저장한다.
 * 예) about.ceoPhotoUrl = 회사소개 대표 사진 주소
 */
@Entity
@Table(name = "site_setting")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SiteSetting {

    @Id
    @Column(name = "setting_key", length = 80)
    private String key;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String value;
}