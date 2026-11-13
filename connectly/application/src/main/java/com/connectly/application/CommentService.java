package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.enums.Role;
import com.connectly.domain.event.CommentLiked;
import com.connectly.domain.event.CommentReplied;
import com.connectly.domain.event.PostCommented;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.model.Comment;
import com.connectly.domain.model.Post;
import com.connectly.domain.model.User;
import com.connectly.domain.port.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
public class CommentService implements AddComment, ListComments, DeleteComment, LikeComment {
    private final CommentRepository comments;
    private final CommentLikeRepository commentLikes;
    private final PostRepository posts;
    private final UserRepository users;
    private final EventPublisher events;

    public CommentService(CommentRepository comments, CommentLikeRepository commentLikes, PostRepository posts,
                          UserRepository users, EventPublisher events) {
        this.comments = comments; this.commentLikes = commentLikes; this.posts = posts;
        this.users = users; this.events = events;
    }

    @Override
    public CommentView add(Long postId, Long userId, Long parentId, String content) {
        Post post = findPost(postId);
        Comment parent = null;
        Long rootId = null;
        if (parentId != null) {
            parent = comments.findById(parentId).orElseThrow(() -> new NotFoundException("Komentar nije pronađen."));
            if (!parent.postId().equals(postId)) throw new DomainException("Komentar ne pripada ovoj objavi.");
            // niti su samo jednog nivoa: odgovor na odgovor ide u istu nit
            rootId = parent.parentId() != null ? parent.parentId() : parent.id();
        }
        Comment saved = comments.save(Comment.reply(postId, rootId, userId, content));

        if (parent == null) {
            events.publish(new PostCommented(postId, userId, post.authorId()));
        } else {
            events.publish(new CommentReplied(saved.id(), postId, userId, parent.authorId()));
            // autor objave se obaveštava samo ako već nije dobio obaveštenje kao autor komentara na koji se odgovara
            if (!post.authorId().equals(parent.authorId()))
                events.publish(new PostCommented(postId, userId, post.authorId()));
        }
        return views(List.of(saved), userId, post.authorId(), isAdmin(userId)).get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentView> list(Long postId, Long viewerId, int page, int size) {
        Post post = findPost(postId);
        return views(comments.findByPostId(postId, Math.max(0, page), Math.max(1, Math.min(size, 50))),
                viewerId, post.authorId(), isAdmin(viewerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentView> replies(Long commentId, Long viewerId, int page, int size) {
        Comment parent = findComment(commentId);
        Post post = findPost(parent.postId());
        return views(comments.findReplies(commentId, Math.max(0, page), Math.max(1, Math.min(size, 50))),
                viewerId, post.authorId(), isAdmin(viewerId));
    }

    @Override
    public void delete(Long commentId, Long userId) {
        Comment c = comments.findById(commentId).orElseThrow(() -> new NotFoundException("Komentar nije pronađen."));
        Post post = findPost(c.postId());
        boolean allowed = c.authorId().equals(userId) || post.authorId().equals(userId) || isAdmin(userId);
        if (!allowed) throw new ForbiddenException("Ne možeš obrisati ovaj komentar.");
        comments.softDelete(commentId);
    }

    @Override
    public LikeResult like(Long commentId, Long userId) {
        Comment c = findComment(commentId);
        findPost(c.postId());
        if (!commentLikes.exists(commentId, userId)) {
            commentLikes.add(commentId, userId);
            events.publish(new CommentLiked(commentId, c.postId(), userId, c.authorId()));
        }
        return new LikeResult(true, commentLikes.count(commentId));
    }

    @Override
    public LikeResult unlike(Long commentId, Long userId) {
        findComment(commentId);
        commentLikes.remove(commentId, userId);
        return new LikeResult(false, commentLikes.count(commentId));
    }

    private Comment findComment(Long commentId) {
        return comments.findById(commentId).orElseThrow(() -> new NotFoundException("Komentar nije pronađen."));
    }

    private Post findPost(Long postId) {
        return posts.findById(postId).orElseThrow(() -> new NotFoundException("Objava nije pronađena."));
    }

    private boolean isAdmin(Long userId) {
        return users.findById(userId).map(u -> u.role() == Role.ADMIN).orElse(false);
    }

    /** Pravi poglede za listu komentara sa po jednim upitom za autore, lajkove i odgovore (bez N+1). */
    private List<CommentView> views(List<Comment> list, Long viewerId, Long postAuthorId, boolean viewerIsAdmin) {
        if (list.isEmpty()) return List.of();
        List<Long> ids = list.stream().map(Comment::id).toList();
        Set<Long> authorIds = new HashSet<>();
        for (Comment c : list) authorIds.add(c.authorId());

        Map<Long, User> authors = new HashMap<>();
        for (User u : users.findByIds(authorIds)) authors.put(u.id(), u);
        Map<Long, Long> likeCounts = commentLikes.countByComments(ids);
        Set<Long> liked = commentLikes.likedBy(ids, viewerId);
        Map<Long, Long> replyCounts = comments.countRepliesByParents(ids);

        return list.stream().map(c -> {
            User author = authors.get(c.authorId());
            boolean canDelete = c.authorId().equals(viewerId) || postAuthorId.equals(viewerId) || viewerIsAdmin;
            return new CommentView(c.id(), c.postId(), c.parentId(), c.authorId(),
                    author == null ? "deleted user" : author.username(),
                    author == null ? null : ImageRules.url(author.profileImage()),
                    c.content(), c.createdAt(), canDelete,
                    likeCounts.getOrDefault(c.id(), 0L), liked.contains(c.id()),
                    replyCounts.getOrDefault(c.id(), 0L));
        }).toList();
    }
}
