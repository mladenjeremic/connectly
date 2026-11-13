package com.connectly.app;

import com.connectly.application.NotificationService;
import com.connectly.domain.enums.NotificationType;
import com.connectly.domain.event.AccountStatusChanged;
import com.connectly.domain.event.CommentLiked;
import com.connectly.domain.event.CommentReplied;
import com.connectly.domain.event.PostCommented;
import com.connectly.domain.event.PostLiked;
import com.connectly.domain.event.UserFollowed;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Domenski događaji se ovde pretvaraju u notifikacije. Listener radi sinhrono, u istoj transakciji
 * kao akcija koja ga je izazvala, pa se notifikacija čuva zajedno sa lajkom/komentarom/praćenjem.
 */
@Component
class NotificationListener {
    private final NotificationService notifications;

    NotificationListener(NotificationService notifications) { this.notifications = notifications; }

    @EventListener
    void onLiked(PostLiked e) {
        notifications.record(e.postAuthorId(), e.actorId(), NotificationType.LIKE, e.postId(), null);
    }

    @EventListener
    void onCommented(PostCommented e) {
        notifications.record(e.postAuthorId(), e.actorId(), NotificationType.COMMENT, e.postId(), null);
    }

    @EventListener
    void onCommentLiked(CommentLiked e) {
        notifications.record(e.commentAuthorId(), e.actorId(), NotificationType.COMMENT_LIKE, e.postId(), null);
    }

    @EventListener
    void onCommentReplied(CommentReplied e) {
        notifications.record(e.repliedToAuthorId(), e.actorId(), NotificationType.COMMENT_REPLY, e.postId(), null);
    }

    @EventListener
    void onFollowed(UserFollowed e) {
        notifications.record(e.followedId(), e.followerId(), NotificationType.FOLLOW, null, null);
    }

    @EventListener
    void onAccountStatusChanged(AccountStatusChanged e) {
        notifications.record(e.userId(), null, NotificationType.ACCOUNT_STATUS, null, e.message());
    }
}
