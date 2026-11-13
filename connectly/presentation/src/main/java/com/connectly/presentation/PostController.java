package com.connectly.presentation;

import com.connectly.application.UseCases.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
class PostController {

    record CreatePostRequest(@NotBlank @Size(max = 2000) String content) {}
    record EditPostRequest(@Size(max = 2000) String content) {}

    private final CreatePost create;
    private final GetPost getPost;
    private final EditPost edit;
    private final DeletePost delete;
    private final GetFeed feed;
    private final GetFollowingFeed followingFeed;
    private final LikePost like;

    PostController(CreatePost create, GetPost getPost, EditPost edit, DeletePost delete,
                   GetFeed feed, GetFollowingFeed followingFeed, LikePost like) {
        this.create = create; this.getPost = getPost; this.edit = edit; this.delete = delete;
        this.feed = feed; this.followingFeed = followingFeed; this.like = like;
    }

    /** Tekst + 0..10 fotografija (multipart/form-data). Ovo koristi React forma. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    PostView createWithImages(@AuthenticationPrincipal Long userId,
                              @RequestParam(value = "content", defaultValue = "") String content,
                              @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {
        return create.create(userId, content, Uploads.many(images));
    }

    /** Samo tekst (JSON), npr. iz Postman-a. */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE) @ResponseStatus(HttpStatus.CREATED)
    PostView createText(@AuthenticationPrincipal Long userId, @Valid @RequestBody CreatePostRequest r) {
        return create.create(userId, r.content(), List.of());
    }

    @GetMapping("/feed")
    List<PostView> feed(@AuthenticationPrincipal Long userId,
                        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return feed.feed(userId, page, size);
    }

    @GetMapping("/following")
    List<PostView> following(@AuthenticationPrincipal Long userId,
                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return followingFeed.followingFeed(userId, page, size);
    }

    @GetMapping("/{id}")
    PostView get(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return getPost.get(id, userId); }

    @PutMapping("/{id}")
    PostView update(@AuthenticationPrincipal Long userId, @PathVariable Long id, @Valid @RequestBody EditPostRequest r) {
        return edit.edit(id, userId, r.content());
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(@AuthenticationPrincipal Long userId, @PathVariable Long id) { delete.delete(id, userId); }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    PostView addImages(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                       @RequestParam("images") List<MultipartFile> images) throws IOException {
        return edit.addImages(id, userId, Uploads.many(images));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    PostView removeImage(@AuthenticationPrincipal Long userId, @PathVariable Long id, @PathVariable Long imageId) {
        return edit.removeImage(id, imageId, userId);
    }

    @PostMapping("/{id}/likes")
    LikeResult like(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return like.like(id, userId); }

    @DeleteMapping("/{id}/likes")
    LikeResult unlike(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return like.unlike(id, userId); }
}
