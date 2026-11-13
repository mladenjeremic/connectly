package com.connectly.app;

import com.connectly.application.*;
import com.connectly.application.UseCases.GetPost;
import com.connectly.domain.port.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Jedino mesto gde se povezuju servisi aplikacionog sloja sa portovima koje implementira infrastruktura. */
@Configuration
class UseCaseConfig {

    @Bean AccountGuard accountGuard(UserRepository users, BanRepository bans) {
        return new AccountGuard(users, bans);
    }

    @Bean AuthService authService(UserRepository users, PasswordHasher hasher, TokenService tokens, AccountGuard guard) {
        return new AuthService(users, hasher, tokens, guard);
    }

    @Bean PostService postService(PostRepository posts, PostImageRepository images, LikeRepository likes,
                                  CommentRepository comments, UserRepository users, FollowRepository follows,
                                  ImageStorage storage, EventPublisher events, BookmarkRepository bookmarks) {
        return new PostService(posts, images, likes, comments, users, follows, storage, events, bookmarks);
    }

    @Bean BookmarkService bookmarkService(BookmarkRepository bookmarks, PostRepository posts, GetPost postService) {
        return new BookmarkService(bookmarks, posts, postService);
    }

    @Bean CommentService commentService(CommentRepository comments, CommentLikeRepository commentLikes,
                                        PostRepository posts, UserRepository users, EventPublisher events) {
        return new CommentService(comments, commentLikes, posts, users, events);
    }

    @Bean SocialService socialService(UserRepository users, FollowRepository follows, PostRepository posts,
                                      EventPublisher events, BlockRepository blocks) {
        return new SocialService(users, follows, posts, events, blocks);
    }

    @Bean UserService userService(UserRepository users, FollowRepository follows, PasswordHasher hasher,
                                  ImageStorage storage, BanRepository bans) {
        return new UserService(users, follows, hasher, storage, bans);
    }

    @Bean StoryService storyService(StoryRepository stories, UserRepository users, FollowRepository follows,
                                    ImageStorage storage, EventPublisher events, BlockRepository blocks) {
        return new StoryService(stories, users, follows, storage, events, blocks);
    }

    @Bean NotificationService notificationService(NotificationRepository notifications, UserRepository users) {
        return new NotificationService(notifications, users);
    }

    @Bean MessageService messageService(ConversationRepository conversations, MessageRepository messages,
                                        UserRepository users, FollowRepository follows, BlockRepository blocks) {
        return new MessageService(conversations, messages, users, follows, blocks);
    }

    @Bean BlockService blockService(BlockRepository blocks, FollowRepository follows, UserRepository users) {
        return new BlockService(blocks, follows, users);
    }

    @Bean ReportService reportService(ReportRepository reports, PostRepository posts, CommentRepository comments) {
        return new ReportService(reports, posts, comments);
    }

    @Bean ModerationService moderationService(UserRepository users, PostRepository posts, PostImageRepository postImages,
                                              CommentRepository comments, ReportRepository reports, BanRepository bans,
                                              AdminActionRepository actions, EventPublisher events) {
        return new ModerationService(users, posts, postImages, comments, reports, bans, actions, events);
    }

    @Bean ImageService imageService(ImageStorage storage) {
        return new ImageService(storage);
    }
}
