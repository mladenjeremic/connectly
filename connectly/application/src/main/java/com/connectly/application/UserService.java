package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.exception.InvalidCredentialsException;
import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.model.User;
import com.connectly.domain.port.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
public class UserService implements UserOps {
    private final UserRepository users;
    private final FollowRepository follows;
    private final PasswordHasher hasher;
    private final ImageStorage storage;
    private final BanRepository bans;

    public UserService(UserRepository users, FollowRepository follows, PasswordHasher hasher,
                       ImageStorage storage, BanRepository bans) {
        this.users = users; this.follows = follows; this.hasher = hasher; this.storage = storage; this.bans = bans;
    }

    @Override
    @Transactional(readOnly = true)
    public MeView me(Long userId) { return toMe(require(userId)); }

    @Override
    public MeView updateProfile(Long userId, UpdateProfileCommand cmd) {
        User u = require(userId);
        String first = clean(cmd.firstName()), last = clean(cmd.lastName()), bio = clean(cmd.bio());
        if (first.isEmpty() || last.isEmpty()) throw new DomainException("Ime i prezime su obavezni.");
        if (first.length() > 60 || last.length() > 60) throw new DomainException("Ime i prezime mogu imati najviše 60 karaktera.");
        if (bio.length() > 300) throw new DomainException("Biografija može imati najviše 300 karaktera.");
        return toMe(users.save(u.withProfile(first, last, bio)));
    }

    @Override
    public MeView updatePhoto(Long userId, ImageUpload photo) {
        User u = require(userId);
        ImageRules.Detected d = ImageRules.inspect(photo);
        String newFile = storage.save(photo.data(), d.extension());
        String oldFile = u.profileImage();
        User saved = users.save(u.withProfileImage(newFile));
        if (oldFile != null && !oldFile.isBlank()) storage.delete(oldFile);
        return toMe(saved);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User u = require(userId);
        if (!hasher.matches(currentPassword, u.passwordHash()))
            throw new InvalidCredentialsException("Trenutna lozinka nije tačna.");
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 72)
            throw new DomainException("Nova lozinka mora imati 8-72 karaktera.");
        users.save(u.withPasswordHash(hasher.hash(newPassword)));
    }

    @Override
    public void deactivate(Long userId, String password) {
        User u = require(userId);
        if (u.isAdmin()) throw new ForbiddenException("Administratorski nalog ne može sam da se deaktivira.");
        if (!hasher.matches(password, u.passwordHash()))
            throw new InvalidCredentialsException("Lozinka nije tačna.");
        bans.deactivateAllForUser(userId);
        users.save(u.withStatus(AccountStatus.DEACTIVATED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummary> search(String query, Long viewerId, int page, int size) {
        String q = query == null ? "" : query.strip();
        if (q.isEmpty()) return List.of();
        return summaries(users.searchPublic(q, Math.max(0, page), clampSize(size)), viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummary> suggestions(Long viewerId, int limit) {
        Set<Long> excluded = new HashSet<>(follows.findFollowedIds(viewerId));
        excluded.add(viewerId);
        return summaries(users.findSuggestions(excluded, Math.max(1, Math.min(limit, 20))), viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummary> followers(Long userId, Long viewerId, int page, int size) {
        require(userId);
        return byIds(follows.findFollowerIds(userId, Math.max(0, page), clampSize(size)), viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummary> following(Long userId, Long viewerId, int page, int size) {
        require(userId);
        return byIds(follows.findFollowingIds(userId, Math.max(0, page), clampSize(size)), viewerId);
    }

    // ---------- pomoćne ----------

    private User require(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("Korisnik nije pronađen."));
    }

    private List<UserSummary> byIds(List<Long> ids, Long viewerId) {
        Map<Long, User> byId = new HashMap<>();
        for (User u : users.findByIds(ids)) byId.put(u.id(), u);
        List<User> ordered = new ArrayList<>();
        for (Long id : ids) { User u = byId.get(id); if (u != null) ordered.add(u); }
        return summaries(ordered, viewerId);
    }

    private List<UserSummary> summaries(List<User> list, Long viewerId) {
        return list.stream().map(u -> new UserSummary(u.id(), u.username(), u.fullName(),
                ImageRules.url(u.profileImage()), follows.exists(viewerId, u.id()), u.id().equals(viewerId))).toList();
    }

    private static MeView toMe(User u) {
        return new MeView(u.id(), u.firstName(), u.lastName(), u.username(), u.email(), u.bio(),
                ImageRules.url(u.profileImage()), u.role(), u.createdAt());
    }

    private static String clean(String s) { return s == null ? "" : s.strip(); }
    private static int clampSize(int size) { return Math.max(1, Math.min(size, 50)); }
}
