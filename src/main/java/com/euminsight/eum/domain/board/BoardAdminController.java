package com.euminsight.eum.domain.board;

import com.euminsight.eum.common.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin/board")
@RequiredArgsConstructor
public class BoardAdminController {

    private final BoardPostRepository boardPostRepository;

    public record BoardRequest(
            String board, String title, String content, String linkUrl,
            Boolean pinned, Boolean published, List<FileRequest> attachments
    ) {}

    public record FileRequest(String name, String url, String fileKey, Long size) {}

    private static final int MAX_ATTACHMENTS = 10;

    /** 관리자 화면 select 박스용 게시판 목록 */
    @GetMapping("/types")
    public List<Map<String, String>> types() {
        return Arrays.stream(BoardType.values())
                .map(t -> Map.of("code", t.getCode(), "label", t.getLabel(),
                        "groupLabel", t.getGroupLabel(), "path", t.getPath()))
                .toList();
    }

    @GetMapping
    public List<BoardPost> list() {
        return boardPostRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public BoardPost get(@PathVariable Long id) {
        return boardPostRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody BoardRequest req) {
        BoardPost post = new BoardPost();
        apply(post, req);
        boardPostRepository.save(post);
        return Map.of("id", post.getId());
    }

    @PutMapping("/{id}")
    @Transactional
    public Map<String, Object> update(@PathVariable Long id, @RequestBody BoardRequest req) {
        BoardPost post = boardPostRepository.findById(id).orElseThrow(NoSuchElementException::new);
        apply(post, req);
        boardPostRepository.save(post);
        return Map.of("id", post.getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        boardPostRepository.deleteById(id);
    }

    private void apply(BoardPost post, BoardRequest req) {
        BoardType board = BoardType.fromCode(req.board());
        if (board == null) throw new IllegalArgumentException("게시판을 선택해 주세요.");

        String title = trim(req.title());
        if (title == null) throw new IllegalArgumentException("제목을 입력해 주세요.");
        if (title.length() > 200) throw new IllegalArgumentException("제목은 200자 이내로 입력해 주세요.");

        String content = HtmlSanitizer.clean(trim(req.content()));
        if (HtmlSanitizer.isBlank(content)) throw new IllegalArgumentException("본문을 입력해 주세요.");

        String linkUrl = trim(req.linkUrl());
        if (linkUrl != null && !linkUrl.matches("(?i)^https?://.+")) {
            throw new IllegalArgumentException("관련 링크는 http:// 또는 https:// 로 시작해야 합니다.");
        }

        post.setBoard(board);
        post.setTitle(title);
        post.setContent(content);
        post.getAttachments().clear();
        post.getAttachments().addAll(toFiles(req.attachments()));
        post.setLinkUrl(linkUrl);
        post.setPinned(Boolean.TRUE.equals(req.pinned()));
        post.setPublished(req.published() == null || req.published());
    }

    private static List<BoardFile> toFiles(List<FileRequest> list) {
        List<BoardFile> files = new ArrayList<>();
        if (list == null) return files;
        if (list.size() > MAX_ATTACHMENTS) {
            throw new IllegalArgumentException("첨부파일은 " + MAX_ATTACHMENTS + "개까지 올릴 수 있습니다.");
        }
        for (FileRequest f : list) {
            String name = trim(f.name());
            String url = trim(f.url());
            if (name == null || url == null || !url.matches("(?i)^https://.+")) {
                throw new IllegalArgumentException("첨부파일 정보가 올바르지 않습니다. 다시 올려 주세요.");
            }
            files.add(BoardFile.builder()
                    .name(name.length() > 255 ? name.substring(name.length() - 255) : name)
                    .url(url)
                    .fileKey(trim(f.fileKey()))
                    .size(f.size() == null ? 0 : f.size())
                    .build());
        }
        return files;
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}