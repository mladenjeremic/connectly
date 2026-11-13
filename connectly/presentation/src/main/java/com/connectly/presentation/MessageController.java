package com.connectly.presentation;

import com.connectly.application.UseCases.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/** Poruke se adresiraju id-jem drugog korisnika, pa klijentu ne trebaju id-jevi razgovora. */
@RestController
@RequestMapping("/api/messages")
class MessageController {

    record SendRequest(@NotBlank @Size(max = 2000) String content) {}

    private final MessageOps messages;
    MessageController(MessageOps messages) { this.messages = messages; }

    @GetMapping("/conversations")
    List<ConversationView> conversations(@AuthenticationPrincipal Long userId,
                                         @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size) {
        return messages.conversations(userId, page, size);
    }

    @GetMapping("/unread-count")
    Map<String, Long> unreadCount(@AuthenticationPrincipal Long userId) {
        return Map.of("count", messages.unreadCount(userId));
    }

    @GetMapping("/with/{otherUserId}")
    List<MessageView> thread(@AuthenticationPrincipal Long userId, @PathVariable Long otherUserId,
                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return messages.thread(userId, otherUserId, page, size);
    }

    @PostMapping("/with/{otherUserId}") @ResponseStatus(HttpStatus.CREATED)
    MessageView send(@AuthenticationPrincipal Long userId, @PathVariable Long otherUserId,
                     @Valid @RequestBody SendRequest r) {
        return messages.send(userId, otherUserId, r.content());
    }

    @PatchMapping("/with/{otherUserId}/read") @ResponseStatus(HttpStatus.NO_CONTENT)
    void markRead(@AuthenticationPrincipal Long userId, @PathVariable Long otherUserId) {
        messages.markRead(userId, otherUserId);
    }
}
