package com.connectly.presentation;

import com.connectly.application.UseCases.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
class BookmarkController {
    private final BookmarkOps bookmarks;
    BookmarkController(BookmarkOps bookmarks) { this.bookmarks = bookmarks; }

    @GetMapping("/saved")
    List<PostView> saved(@AuthenticationPrincipal Long userId,
                         @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return bookmarks.saved(userId, page, size);
    }

    @PostMapping("/{id}/bookmark")
    SaveResult save(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return bookmarks.save(id, userId); }

    @DeleteMapping("/{id}/bookmark")
    SaveResult unsave(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return bookmarks.unsave(id, userId); }
}
