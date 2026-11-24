package com.euminsight.eum.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DB 컬럼 보정 (서버 시작 시 1회 확인, 이미 맞으면 아무것도 안 함)
 * - board_post.board 가 MariaDB ENUM 으로 만들어진 경우 VARCHAR(20)으로 변경
 *   → 새 게시판(주요 고객사 등)을 추가해도 저장 오류가 나지 않게 함
 */
@Slf4j
@Order(0)
@Component
@RequiredArgsConstructor
public class SchemaFixer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        toVarchar("board_post", "board", 20);
    }

    private void toVarchar(String table, String column, int length) {
        try {
            List<String> types = jdbcTemplate.queryForList(
                    "SELECT DATA_TYPE FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    String.class, table, column);
            if (types.isEmpty() || !"enum".equalsIgnoreCase(types.get(0))) return;
            jdbcTemplate.execute("ALTER TABLE " + table + " MODIFY " + column + " VARCHAR(" + length + ") NOT NULL");
            log.info(">>> {}.{} 컬럼을 ENUM → VARCHAR({})로 변경했습니다.", table, column, length);
        } catch (Exception e) {
            // 로컬 H2 등 information_schema 구조가 다른 DB에서는 건너뜀
            log.debug("SchemaFixer 건너뜀: {}", e.getMessage());
        }
    }
}