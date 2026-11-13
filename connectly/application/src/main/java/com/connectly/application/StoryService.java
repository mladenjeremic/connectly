package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.event.StoryReplied;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.model.Story;
import com.connectly.domain.model.User;
import com.connectly.domain.port.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Transactional
public class StoryService implements StoryOps {
    private final StoryRepository stories;
    private final UserRepository users;
    private final FollowRepository follows;
    private final ImageStorage storage;
    private final EventPublisher events;
    private final BlockRepository blocks;

    public StoryService(StoryRepository stories, UserRepository users, FollowRepository follows,
                        ImageStorage storage, EventPublisher events, BlockRepository blocks) {
        this.stories = stories; this.users = users; this.follows = follows; this.storage = storage;
        this.events = events; this.blocks = blocks;
    }

    @Override
    public StoryView create(Long userId, ImageUpload image) {
        ImageRules.Detected d = ImageRules.inspect(image);
        String fileName = storage.save(image.data(), d.extension());
        Story saved = stories.save(Story.create(userId, fileName));
        return toView(saved, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoryGroup> feed(Long viewerId) {
        List<Long> authorIds = new ArrayList<>(follows.findFollowedIds(viewerId));
        authorIds.removeAll(blocks.findBlockedIds(viewerId));
        authorIds.removeAll(blocks.findBlockerIds(viewerId));
        authorIds.add(viewerId);
        List<Story> active = stories.findActiveByAuthors(authorIds, Instant.now());
        if (active.isEmpty()) return List.of();

        Map<Long, List<Story>> byAuthor = new LinkedHashMap<>();
        for (Story s : active) byAuthor.computeIfAbsent(s.authorId(), k -> new ArrayList<>()).add(s);

        Map<Long, User> authors = new HashMap<>();
        for (User u : users.findByIds(byAuthor.keySet())) authors.put(u.id(), u);

        List<StoryGroup> groups = new ArrayList<>();
        for (Map.Entry<Long, List<Story>> e : byAuthor.entrySet()) {
            User a = authors.get(e.getKey());
            if (a == null || !a.isActive()) continue;
            groups.add(new StoryGroup(a.id(), a.username(), ImageRules.url(a.profileImage()),
                    a.id().equals(viewerId), e.getValue().stream().map(s -> toView(s, viewerId)).toList()));
        }
        // moje priče prve, ostale po najnovijoj objavljenoj priči
        groups.sort((g1, g2) -> {
            if (g1.ownGroup() != g2.ownGroup()) return g1.ownGroup() ? -1 : 1;
            Instant t1 = g1.stories().get(g1.stories().size() - 1).createdAt();
            Instant t2 = g2.stories().get(g2.stories().size() - 1).createdAt();
            return t2.compareTo(t1);
        });
        return groups;
    }

    @Override
    public void delete(Long storyId, Long userId) {
        Story s = stories.findById(storyId).orElseThrow(() -> new NotFoundException("Priča nije pronađena."));
        if (!s.authorId().equals(userId)) throw new ForbiddenException("Možeš obrisati samo svoju priču.");
        stories.delete(storyId);
        storage.delete(s.fileName());
    }

    @Override
    public void reply(Long storyId, Long userId, String content) {
        Story s = stories.findById(storyId).orElseThrow(() -> new NotFoundException("Priča nije pronađena."));
        if (!s.expiresAt().isAfter(Instant.now())) throw new NotFoundException("Priča je istekla.");
        if (s.authorId().equals(userId)) throw new DomainException("Ne možeš odgovoriti na sopstvenu priču.");
        if (blocks.existsEither(userId, s.authorId()))
            throw new ForbiddenException("Ne možeš odgovoriti na ovu priču.");
        if (!follows.exists(userId, s.authorId()))
            throw new ForbiddenException("Odgovor možeš poslati samo na priče ljudi koje pratiš.");
        String text = content == null ? "" : content.strip();
        if (text.isEmpty() || text.length() > 500) throw new DomainException("Odgovor mora imati 1-500 karaktera.");
        events.publish(new StoryReplied(storyId, userId, s.authorId(), text));
    }

    private StoryView toView(Story s, Long viewerId) {
        return new StoryView(s.id(), ImageRules.url(s.fileName()), s.createdAt(), s.expiresAt(),
                s.authorId().equals(viewerId));
    }
}
