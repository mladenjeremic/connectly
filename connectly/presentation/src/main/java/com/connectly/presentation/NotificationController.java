package com.connectly.presentation;

import com.connectly.application.UseCases.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
class NotificationController {
    private final NotificationOps notifications;
    NotificationController(NotificationOps notifications) { this.notifications = notifications; }

    @GetMapping
    List<NotificationView> list(@AuthenticationPrincipal Long userId,
                                @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size) {
        return notifications.list(userId, page, size);
    }

    @GetMapping("/unread-count")
    Map<String, Long> unreadCount(@AuthenticationPrincipal Long userId) {
        return Map.of("count", notifications.unreadCount(userId));
    }

    @PatchMapping("/{id}/read") @ResponseStatus(HttpStatus.NO_CONTENT)
    void markRead(@AuthenticationPrincipal Long userId, @PathVariable Long id) { notifications.markRead(id, userId); }

    @PatchMapping("/read-all") @ResponseStatus(HttpStatus.NO_CONTENT)
    void markAllRead(@AuthenticationPrincipal Long userId) { notifications.markAllRead(userId); }
}
