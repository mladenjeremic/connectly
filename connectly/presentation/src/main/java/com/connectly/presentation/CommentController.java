package com.connectly.presentation;
import com.connectly.application.UseCases.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
class CommentController {

    /** parentId je opcioni: ako postoji, komentar je odgovor na taj komentar. */
    record CommentRequest(@NotBlank @Size(max = 1000) String content, Long parentId) {}

    private final AddComment add;
    private final ListComments list;
    private final DeleteComment delete;
    private final LikeComment like;

    CommentController(AddComment add, ListComments list, DeleteComment delete, LikeComment like) {
        this.add = add; this.list = list; this.delete = delete; this.like = like;
    }

    @GetMapping("/api/posts/{postId}/comments")
    List<CommentView> list(@AuthenticationPrincipal Long userId, @PathVariable Long postId,
                           @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return list.list(postId, userId, page, size);
    }

    @PostMapping("/api/posts/{postId}/comments") @ResponseStatus(HttpStatus.CREATED)
    CommentView add(@AuthenticationPrincipal Long userId, @PathVariable Long postId,
                    @Valid @RequestBody CommentRequest r) {
        return add.add(postId, userId, r.parentId(), r.content());
    }

    @GetMapping("/api/comments/{id}/replies")
    List<CommentView> replies(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                              @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return list.replies(id, userId, page, size);
    }

    @DeleteMapping("/api/comments/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) { delete.delete(id, userId); }

    @PostMapping("/api/comments/{id}/likes")
    LikeResult like(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return like.like(id, userId); }

    @DeleteMapping("/api/comments/{id}/likes")
    LikeResult unlike(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return like.unlike(id, userId); }
}
