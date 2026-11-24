package com.euminsight.eum.domain.menu;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 관리자: 메뉴·게시판·국가 탭 이름(한/영), 표시 여부, 순서 관리
 */
@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
public class MenuAdminController {

    private final MenuService menuService;

    @GetMapping
    public List<MenuService.AdminRow> list() {
        return menuService.adminRows();
    }

    @PutMapping
    public Map<String, Object> save(@RequestBody List<MenuService.UpdateItem> items) {
        menuService.update(items);
        return Map.of("ok", true);
    }
}