package com.connectly.presentation;

import com.connectly.application.UseCases.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/blocks")
class BlockController {
    private final BlockOps blocks;
    BlockController(BlockOps blocks) { this.blocks = blocks; }

    @GetMapping
    List<UserSummary> list(@AuthenticationPrincipal Long userId) { return blocks.blocked(userId); }

    @PostMapping("/{targetId}")
    ResponseEntity<Void> block(@AuthenticationPrincipal Long userId, @PathVariable Long targetId) {
        blocks.block(userId, targetId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{targetId}")
    ResponseEntity<Void> unblock(@AuthenticationPrincipal Long userId, @PathVariable Long targetId) {
        blocks.unblock(userId, targetId);
        return ResponseEntity.noContent().build();
    }
}
