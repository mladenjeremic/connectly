package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.event.PostLiked;
import com.connectly.domain.exception.DomainException;
import com.connectly.domain.exception.ForbiddenException;
import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.model.Post;
import com.connectly.domain.model.PostImage;
import com.connectly.domain.model.User;
import com.connectly.domain.port.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
public class PostService implements CreatePost, GetPost, EditPost, DeletePost, GetFeed, GetFollowingFeed, GetUserPosts, LikePost {
    private final PostRepository posts;
    private final PostImageRepository images;
    private final LikeRepository likes;
    private final CommentRepository comments;
    private final UserRepository users;
    private final FollowRepository follows;
    private final ImageStorage storage;
    private final EventPublisher events;
    private final BookmarkRepository bookmarks;

    public PostService(PostRepository posts, PostImageRepository images, LikeRepository likes,
                       CommentRepository comments, UserRepository users, FollowRepository follows,
                       ImageStorage storage, EventPublisher events, BookmarkRepository bookmarks) {
        this.posts = posts; this.images = images; this.likes = likes; this.comments = comments;
        this.users = users; this.follows = follows; this.storage = storage; this.events = events;
        this.bookmarks = bookmarks;
    }

    // ---------- kreiranje / izmena / brisanje ----------

    @Override
    public PostView create(Long authorId, String content, List<ImageUpload> uploads) {
        List<ImageUpload> ups = uploads == null ? List.of() : uploads;
        String text = content == null ? "" : content.strip();
        if (text.isEmpty() && ups.isEmpty())
            throw new DomainException("Objava mora imati tekst ili bar jednu fotografiju.");
        if (ups.size() > ImageRules.MAX_IMAGES_PER_POST)
            throw new DomainException("Najviše " + ImageRules.MAX_IMAGES_PER_POST + " fotografija po objavi.");
        List<ImageRules.Detected> detected = ups.stream().map(ImageRules::inspect).toList();  // prvo sve proveri

        Post saved = posts.save(Post.create(authorId, text));
        for (int i = 0; i < ups.size(); i++) storeImage(saved.id(), ups.get(i), detected.get(i), i);
        return view(saved, authorId);
    }

    @Override
    public PostView edit(Long postId, Long userId, String content) {
        Post post = requireOwned(postId, userId);
        String text = content == null ? "" : content.strip();
        if (text.isEmpty() && images.countByPostId(postId) == 0)
            throw new DomainException("Objava mora imati tekst ili bar jednu fotografiju.");
        return view(posts.save(post.edit(text)), userId);
    }

    @Override
    public PostView addImages(Long postId, Long userId, List<ImageUpload> uploads) {
        Post post = requireOwned(postId, userId);
        List<ImageUpload> ups = uploads == null ? List.of() : uploads;
        if (ups.isEmpty()) throw new DomainException("Nijedna fotografija nije poslata.");
        int existing = images.countByPostId(postId);
        if (existing + ups.size() > ImageRules.MAX_IMAGES_PER_POST)
            throw new DomainException("Najviše " + ImageRules.MAX_IMAGES_PER_POST + " fotografija po objavi.");
        List<ImageRules.Detected> detected = ups.stream().map(ImageRules::inspect).toList();
        for (int i = 0; i < ups.size(); i++) storeImage(postId, ups.get(i), detected.get(i), existing + i);
        return view(posts.save(post.edit(post.content())), userId);
    }

    @Override
    public PostView removeImage(Long postId, Long imageId, Long userId) {
        Post post = requireOwned(postId, userId);
        PostImage img = images.findById(imageId)
                .filter(i -> i.postId().equals(postId))
                .orElseThrow(() -> new NotFoundException("Fotografija nije pronađena."));
        if (post.content().isBlank() && images.countByPostId(postId) <= 1)
            throw new DomainException("Objava bez teksta mora zadržati bar jednu fotografiju.");
        images.delete(imageId);
        storage.delete(img.fileName());
        return view(post, userId);
    }

    @Override
    public void delete(Long postId, Long userId) {
        requireOwned(postId, userId);
        posts.softDelete(postId);
    }

    // ---------- čitanje ----------

    @Override
    @Transactional(readOnly = true)
    public PostView get(Long postId, Long viewerId) {
        return view(find(postId), viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostView> feed(Long viewerId, int page, int size) {
        return views(posts.findLatest(page(page), size(size)), viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostView> followingFeed(Long viewerId, int page, int size) {
        List<Long> authors = new ArrayList<>(follows.findFollowedIds(viewerId));
        authors.add(viewerId);
        return views(posts.findByAuthors(authors, page(page), size(size)), viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostView> byUser(String username, Long viewerId, int page, int size) {
        User author = users.findByUsername(username).orElseThrow(() -> new NotFoundException("Korisnik nije pronađen."));
        return views(posts.findByAuthors(List.of(author.id()), page(page), size(size)), viewerId);
    }

    // ---------- lajkovi ----------

    @Override
    public LikeResult like(Long postId, Long userId) {
        Post p = find(postId);
        if (!likes.exists(postId, userId)) {
            likes.add(postId, userId);
            events.publish(new PostLiked(postId, userId, p.authorId()));
        }
        return new LikeResult(true, likes.count(postId));
    }

    @Override
    public LikeResult unlike(Long postId, Long userId) {
        find(postId);
        likes.remove(postId, userId);
        return new LikeResult(false, likes.count(postId));
    }

    // ---------- pomoćne metode ----------

    private Post find(Long postId) {
        return posts.findById(postId).orElseThrow(() -> new NotFoundException("Objava nije pronađena."));
    }

    private Post requireOwned(Long postId, Long userId) {
        Post post = find(postId);
        if (!post.authorId().equals(userId)) throw new ForbiddenException("Možeš menjati samo svoje objave.");
        return post;
    }

    private void storeImage(Long postId, ImageUpload up, ImageRules.Detected d, int order) {
        String fileName = storage.save(up.data(), d.extension());
        images.save(new PostImage(null, postId, fileName, d.contentType(), up.data().length, order));
    }

    private PostView view(Post p, Long viewerId) { return views(List.of(p), viewerId).get(0); }

    private List<PostView> views(List<Post> list, Long viewerId) {
        if (list.isEmpty()) return List.of();
        Set<Long> authorIds = new HashSet<>();
        List<Long> postIds = new ArrayList<>();
        for (Post p : list) { authorIds.add(p.authorId()); postIds.add(p.id()); }
        Set<Long> savedIds = bookmarks.savedAmong(postIds, viewerId);

        Map<Long, User> authors = new HashMap<>();
        for (User u : users.findByIds(authorIds)) authors.put(u.id(), u);

        Map<Long, List<PostImage>> imagesByPost = new HashMap<>();
        for (PostImage img : images.findByPostIds(postIds))
            imagesByPost.computeIfAbsent(img.postId(), k -> new ArrayList<>()).add(img);

        List<PostView> result = new ArrayList<>();
        for (Post p : list) {
            User a = authors.get(p.authorId());
            List<ImageView> imgs = imagesByPost.getOrDefault(p.id(), List.of()).stream()
                    .sorted(Comparator.comparingInt(PostImage::sortOrder))
                    .map(i -> new ImageView(i.id(), ImageRules.url(i.fileName()))).toList();
            result.add(new PostView(p.id(), p.authorId(),
                    a == null ? "deleted user" : a.username(),
                    a == null ? "deleted user" : a.fullName(),
                    a == null ? null : ImageRules.url(a.profileImage()),
                    p.content(), imgs, p.createdAt(), p.updatedAt(),
                    likes.count(p.id()), comments.count(p.id()), likes.exists(p.id(), viewerId),
                    p.authorId().equals(viewerId), savedIds.contains(p.id())));
        }
        return result;
    }

    private static int page(int page) { return Math.max(0, page); }
    private static int size(int size) { return Math.max(1, Math.min(size, 50)); }
}
