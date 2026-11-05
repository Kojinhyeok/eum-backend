package com.euminsight.eum.controller;

import com.euminsight.eum.domain.board.BoardPost;
import com.euminsight.eum.domain.board.BoardPostRepository;
import com.euminsight.eum.domain.board.BoardType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.IntStream;

/**
 * 인사이트(화장품 연구동향 / 전문가 칼럼), 공지사항(회사공지 / 행사 및 세미나) 게시판 화면
 *  - 목록: /insights/research, /insights/column, /notice/company, /notice/event
 *  - 상세: 위 주소 + /{id}
 */
@Controller
@RequiredArgsConstructor
public class BoardPageController {

    private static final int PAGE_SIZE = 10;

    private final BoardPostRepository boardPostRepository;

    @GetMapping({"/insights/{slug}", "/notice/{slug}"})
    public String list(@PathVariable String slug,
                       @RequestParam(defaultValue = "0") int page,
                       jakarta.servlet.http.HttpServletRequest request,
                       Model model) {
        BoardType board = resolve(request.getRequestURI(), slug);

        Page<BoardPost> result = boardPostRepository.findByBoardAndPublishedTrueOrderByPinnedDescCreatedAtDesc(
                board, PageRequest.of(Math.max(page, 0), PAGE_SIZE));

        addCommon(model, board);
        model.addAttribute("posts", result.getContent());
        model.addAttribute("hasPosts", result.hasContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("hasPrev", result.hasPrevious());
        model.addAttribute("hasNext", result.hasNext());
        model.addAttribute("pageNumbers", IntStream.range(0, result.getTotalPages()).boxed().toList());
        return "board-list";
    }

    @GetMapping({"/insights/{slug}/{id}", "/notice/{slug}/{id}"})
    public String detail(@PathVariable String slug,
                         @PathVariable Long id,
                         jakarta.servlet.http.HttpServletRequest request,
                         Model model) {
        BoardType board = resolve(request.getRequestURI(), slug);

        boardPostRepository.increaseViewCount(id);
        BoardPost post = boardPostRepository.findByIdAndBoardAndPublishedTrue(id, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        addCommon(model, board);
        model.addAttribute("post", post);
        return "board-detail";
    }

    private BoardType resolve(String uri, String slug) {
        String group = uri.startsWith("/notice/") ? "notice" : "insights";
        BoardType board = BoardType.fromPath(group, slug);
        if (board == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return board;
    }

    private void addCommon(Model model, BoardType board) {
        model.addAttribute("board", board);
        model.addAttribute("isInsights", "insights".equals(board.getGroup()));
        // 헤더 메뉴 활성화 값 (layout.html header(active))
        model.addAttribute("activeMenu", "insights".equals(board.getGroup()) ? "insights" : "notice");
    }
}