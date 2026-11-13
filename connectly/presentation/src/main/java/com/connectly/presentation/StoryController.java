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
@RequestMapping("/api/stories")
class StoryController {
    record ReplyRequest(@NotBlank @Size(max = 500) String content) {}

    private final StoryOps stories;
    StoryController(StoryOps stories) { this.stories = stories; }

    @GetMapping
    List<StoryGroup> feed(@AuthenticationPrincipal Long userId) { return stories.feed(userId); }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    StoryView create(@AuthenticationPrincipal Long userId, @RequestParam("image") MultipartFile image) throws IOException {
        return stories.create(userId, Uploads.one(image));
    }

    @PostMapping("/{id}/replies") @ResponseStatus(HttpStatus.NO_CONTENT)
    void reply(@AuthenticationPrincipal Long userId, @PathVariable Long id, @Valid @RequestBody ReplyRequest r) {
        stories.reply(id, userId, r.content());
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) { stories.delete(id, userId); }
}
