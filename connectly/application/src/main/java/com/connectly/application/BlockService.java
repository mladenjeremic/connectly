package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.model.User;
import com.connectly.domain.port.BlockRepository;
import com.connectly.domain.port.FollowRepository;
import com.connectly.domain.port.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Blokiranje: blokirani i onaj ko blokira ne mogu da se prate, šalju poruke ni odgovaraju na priče
 * jedan drugom. Pri blokiranju se obaveštenje o praćenju poništava u oba smera.
 */
@Transactional
public class BlockService implements BlockOps {
    private final BlockRepository blocks;
    private final FollowRepository follows;
    private final UserRepository users;

    public BlockService(BlockRepository blocks, FollowRepository follows, UserRepository users) {
        this.blocks = blocks; this.follows = follows; this.users = users;
    }

    @Override
    public void block(Long userId, Long targetId) {
        if (userId.equals(targetId)) throw new DomainException("Ne možeš blokirati sam sebe.");
        User target = users.findById(targetId).orElseThrow(() -> new NotFoundException("Korisnik nije pronađen."));
        if (target.isAdmin()) throw new DomainException("Administratora nije moguće blokirati.");
        if (!blocks.exists(userId, targetId)) blocks.add(userId, targetId);
        follows.remove(userId, targetId);
        follows.remove(targetId, userId);
    }

    @Override
    public void unblock(Long userId, Long targetId) {
        blocks.remove(userId, targetId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummary> blocked(Long userId) {
        List<Long> ids = blocks.findBlockedIds(userId);
        if (ids.isEmpty()) return List.of();
        Map<Long, User> byId = new HashMap<>();
        for (User u : users.findByIds(ids)) byId.put(u.id(), u);
        List<UserSummary> result = new ArrayList<>();
        for (Long id : ids) {
            User u = byId.get(id);
            if (u != null) result.add(new UserSummary(u.id(), u.username(), u.fullName(),
                    ImageRules.url(u.profileImage()), false, false));
        }
        return result;
    }
}
