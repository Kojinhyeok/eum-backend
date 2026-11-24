package com.euminsight.eum.domain.menu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 서버 시작 시 menu_setting 에 없는 메뉴 항목만 기본값으로 추가한다.
 * (관리자가 바꾼 값은 유지)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuSeeder implements CommandLineRunner {

    private final MenuService menuService;

    @Override
    public void run(String... args) {
        int added = menuService.seedMissing();
        if (added > 0) log.info(">>> 메뉴 설정 기본값 {}건 추가됨", added);
    }
}