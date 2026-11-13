package com.connectly.application;

import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.enums.AdminActionType;
import com.connectly.domain.enums.NotificationType;
import com.connectly.domain.enums.ReportStatus;
import com.connectly.domain.enums.ReportTargetType;
import com.connectly.domain.enums.Role;
import com.connectly.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Ulazni (use case) interfejsi i DTO-i aplikacionog sloja. Kontroleri zavise samo od ovoga. */
public final class UseCases {
    private UseCases() {}

    // ---- zajedničko ----
    public record ImageUpload(byte[] data, String contentType) {}
    public record ImageData(byte[] bytes, String contentType) {}
    public interface LoadImage { ImageData load(String fileName); }
    /** Koristi ga JWT filter: vraća nalog samo ako sme da koristi zaštićene funkcije (uz automatsko skidanje isteklih zabrana). */
    public interface ResolveAccount { Optional<User> resolve(Long userId); }

    // ---- auth ----
    public record RegisterCommand(String firstName, String lastName, String username, String email, String password) {}
    public record LoginCommand(String email, String password) {}
    public record AuthResult(String token, Long userId, String username, Role role) {}
    public interface Register { AuthResult register(RegisterCommand cmd); }
    public interface Login { AuthResult login(LoginCommand cmd); }

    // ---- korisnici / profil ----
    public record UserSummary(Long id, String username, String fullName, String profileImageUrl,
                              boolean followedByMe, boolean self) {}
    public record MeView(Long id, String firstName, String lastName, String username, String email, String bio,
                         String profileImageUrl, Role role, Instant createdAt) {}
    public record UpdateProfileCommand(String firstName, String lastName, String bio) {}
    public interface UserOps {
        MeView me(Long userId);
        MeView updateProfile(Long userId, UpdateProfileCommand cmd);
        MeView updatePhoto(Long userId, ImageUpload photo);
        void changePassword(Long userId, String currentPassword, String newPassword);
        void deactivate(Long userId, String password);
        List<UserSummary> search(String query, Long viewerId, int page, int size);
        List<UserSummary> suggestions(Long viewerId, int limit);
        List<UserSummary> followers(Long userId, Long viewerId, int page, int size);
        List<UserSummary> following(Long userId, Long viewerId, int page, int size);
    }

    public record ProfileView(Long id, String firstName, String lastName, String username, String bio,
                              String profileImageUrl, Instant createdAt, long followerCount, long followingCount,
                              long postCount, boolean followedByMe, boolean ownProfile, boolean blockedByMe) {}
    public record FollowResult(boolean following, long followerCount) {}
    public interface GetProfile { ProfileView get(String username, Long viewerId); }
    public interface FollowUser {
        FollowResult follow(Long followerId, Long targetId);
        FollowResult unfollow(Long followerId, Long targetId);
    }

    // ---- objave ----
    public record ImageView(Long id, String url) {}
    public record PostView(Long id, Long authorId, String authorUsername, String authorFullName,
                           String authorProfileImageUrl, String content, List<ImageView> images,
                           Instant createdAt, Instant updatedAt, long likeCount, long commentCount,
                           boolean likedByMe, boolean ownPost, boolean savedByMe) {}
    public record LikeResult(boolean liked, long likeCount) {}
    public interface CreatePost { PostView create(Long authorId, String content, List<ImageUpload> images); }
    public interface GetPost { PostView get(Long postId, Long viewerId); }
    public interface EditPost {
        PostView edit(Long postId, Long userId, String content);
        PostView addImages(Long postId, Long userId, List<ImageUpload> images);
        PostView removeImage(Long postId, Long imageId, Long userId);
    }
    public interface DeletePost { void delete(Long postId, Long userId); }
    public interface GetFeed { List<PostView> feed(Long viewerId, int page, int size); }
    public interface GetFollowingFeed { List<PostView> followingFeed(Long viewerId, int page, int size); }
    public interface GetUserPosts { List<PostView> byUser(String username, Long viewerId, int page, int size); }
    public interface LikePost {
        LikeResult like(Long postId, Long userId);
        LikeResult unlike(Long postId, Long userId);
    }

    // ---- sačuvane objave ----
    public record SaveResult(boolean saved) {}
    public interface BookmarkOps {
        SaveResult save(Long postId, Long userId);
        SaveResult unsave(Long postId, Long userId);
        List<PostView> saved(Long userId, int page, int size);
    }

    // ---- komentari ----
    /** parentId je null za komentar prvog nivoa; replyCount se popunjava samo za komentare prvog nivoa. */
    public record CommentView(Long id, Long postId, Long parentId, Long authorId, String authorUsername,
                              String authorProfileImageUrl, String content, Instant createdAt, boolean canDelete,
                              long likeCount, boolean likedByMe, long replyCount) {}
    public interface AddComment { CommentView add(Long postId, Long userId, Long parentId, String content); }
    public interface ListComments {
        List<CommentView> list(Long postId, Long viewerId, int page, int size);
        List<CommentView> replies(Long commentId, Long viewerId, int page, int size);
    }
    public interface DeleteComment { void delete(Long commentId, Long userId); }
    public interface LikeComment {
        LikeResult like(Long commentId, Long userId);
        LikeResult unlike(Long commentId, Long userId);
    }

    // ---- priče (stories) ----
    public record StoryView(Long id, String imageUrl, Instant createdAt, Instant expiresAt, boolean ownStory) {}
    public record StoryGroup(Long userId, String username, String profileImageUrl, boolean ownGroup,
                             List<StoryView> stories) {}
    public interface StoryOps {
        StoryView create(Long userId, ImageUpload image);
        List<StoryGroup> feed(Long viewerId);
        void delete(Long storyId, Long userId);
        /** Odgovor na tuđu priču; autor priče ga dobija kao obaveštenje (kasnije može u direktnu poruku). */
        void reply(Long storyId, Long userId, String content);
    }

    // ---- blokiranje ----
    public interface BlockOps {
        void block(Long userId, Long targetId);
        void unblock(Long userId, Long targetId);
        List<UserSummary> blocked(Long userId);
    }

    // ---- poruke ----
    public record MessageView(Long id, Long senderId, String content, boolean mine, boolean storyReply,
                              Instant createdAt, boolean read) {}
    public record ConversationView(Long id, UserSummary user, String lastMessage, boolean lastMessageMine,
                                   Instant lastMessageAt, long unreadCount) {}
    public interface MessageOps {
        MessageView send(Long senderId, Long recipientId, String content);
        List<ConversationView> conversations(Long userId, int page, int size);
        /** Poruke sa jednim korisnikom, od najstarije ka najnovijoj (prazno ako razgovor ne postoji). */
        List<MessageView> thread(Long userId, Long otherUserId, int page, int size);
        void markRead(Long userId, Long otherUserId);
        long unreadCount(Long userId);
    }

    // ---- notifikacije ----
    public record NotificationView(Long id, NotificationType type, Long actorId, String actorUsername,
                                   String actorProfileImageUrl, Long postId, String message, boolean read,
                                   Instant createdAt) {}
    public interface NotificationOps {
        List<NotificationView> list(Long userId, int page, int size);
        long unreadCount(Long userId);
        void markRead(Long notificationId, Long userId);
        void markAllRead(Long userId);
    }

    // ---- prijave sadržaja ----
    public record ReportReceipt(Long id, ReportStatus status) {}
    public interface ReportOps {
        ReportReceipt create(Long reporterId, ReportTargetType type, Long targetId, String category, String description);
    }

    // ---- administracija ----
    public record DashboardView(long totalUsers, long activeUsers, long suspendedUsers, long bannedUsers,
                                long totalPosts, long pendingReports) {}
    public record BanView(Long id, String reason, Instant startsAt, Instant endsAt, boolean active,
                          boolean permanent, String adminUsername) {}
    public record AdminUserView(Long id, String firstName, String lastName, String username, String email,
                                String profileImageUrl, Role role, AccountStatus status, Instant createdAt,
                                long postCount, BanView activeBan) {}
    public record AdminActionView(Long id, Long adminId, String adminUsername, Long targetUserId,
                                  String targetUsername, AdminActionType actionType, String reason,
                                  String metadata, Instant createdAt) {}
    public record AdminUserDetail(AdminUserView user, List<BanView> bans, List<AdminActionView> actions) {}
    public record ReportView(Long id, Long reporterId, String reporterUsername, ReportTargetType targetType,
                             Long targetId, Long postId, Long targetAuthorId, String targetAuthorUsername,
                             String preview, String imageUrl, boolean targetAvailable, String category,
                             String description, ReportStatus status, String reviewedByUsername,
                             Instant reviewedAt, Instant createdAt) {}
    public enum ReportDecision { DISMISS, REVIEW, REMOVE_CONTENT }

    public interface ModerationOps {
        DashboardView dashboard();
        List<AdminUserView> users(String query, AccountStatus status, int page, int size);
        AdminUserDetail userDetail(Long userId);
        AdminUserView ban(Long adminId, Long userId, int days, String reason);
        AdminUserView permanentBan(Long adminId, Long userId, String reason);
        AdminUserView unban(Long adminId, Long userId, String reason);
        AdminUserView deactivate(Long adminId, Long userId, String reason);
        AdminUserView reactivate(Long adminId, Long userId, String reason);
        void removePost(Long adminId, Long postId, String reason);
        List<ReportView> reports(ReportStatus status, int page, int size);
        ReportView handleReport(Long adminId, Long reportId, ReportDecision decision, String note);
        List<AdminActionView> auditLog(int page, int size);
        /** Poziva periodični job: skida istekle privremene zabrane i obaveštava korisnike. */
        int expireBans();
    }
}
