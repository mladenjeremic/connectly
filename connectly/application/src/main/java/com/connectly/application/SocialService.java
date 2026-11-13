package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.event.UserFollowed;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.model.User;
import com.connectly.domain.port.*;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class SocialService implements GetProfile, FollowUser {
    private final UserRepository users;
    private final FollowRepository follows;
    private final PostRepository posts;
    private final EventPublisher events;
    private final BlockRepository blocks;

    public SocialService(UserRepository users, FollowRepository follows, PostRepository posts,
                         EventPublisher events, BlockRepository blocks) {
        this.users = users; this.follows = follows; this.posts = posts; this.events = events; this.blocks = blocks;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileView get(String username, Long viewerId) {
        User u = users.findByUsername(username).orElseThrow(() -> new NotFoundException("Korisnik nije pronađen."));
        return new ProfileView(u.id(), u.firstName(), u.lastName(), u.username(), u.bio(),
                ImageRules.url(u.profileImage()), u.createdAt(),
                follows.countFollowers(u.id()), follows.countFollowing(u.id()), posts.countByAuthor(u.id()),
                follows.exists(viewerId, u.id()), u.id().equals(viewerId), blocks.exists(viewerId, u.id()));
    }

    @Override
    public FollowResult follow(Long followerId, Long targetId) {
        if (followerId.equals(targetId)) throw new DomainException("Ne možeš pratiti sam sebe.");
        User target = users.findById(targetId).orElseThrow(() -> new NotFoundException("Korisnik nije pronađen."));
        if (!target.isActive()) throw new NotFoundException("Korisnik nije pronađen.");
        if (blocks.existsEither(followerId, targetId)) throw new ForbiddenException("Ne možeš pratiti ovog korisnika.");
        if (!follows.exists(followerId, targetId)) {
            follows.add(followerId, targetId);
            events.publish(new UserFollowed(followerId, targetId));
        }
        return new FollowResult(true, follows.countFollowers(targetId));
    }

    @Override
    public FollowResult unfollow(Long followerId, Long targetId) {
        follows.remove(followerId, targetId);
        return new FollowResult(false, follows.countFollowers(targetId));
    }
}
