package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.port.BookmarkRepository;
import com.connectly.domain.port.PostRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

/** Sačuvane objave (privatna lista korisnika). */
@Transactional
public class BookmarkService implements BookmarkOps {
    private final BookmarkRepository bookmarks;
    private final PostRepository posts;
    private final GetPost getPost;

    public BookmarkService(BookmarkRepository bookmarks, PostRepository posts, GetPost getPost) {
        this.bookmarks = bookmarks; this.posts = posts; this.getPost = getPost;
    }

    @Override
    public SaveResult save(Long postId, Long userId) {
        posts.findById(postId).orElseThrow(() -> new NotFoundException("Objava nije pronađena."));
        if (!bookmarks.exists(postId, userId)) bookmarks.add(postId, userId);
        return new SaveResult(true);
    }

    @Override
    public SaveResult unsave(Long postId, Long userId) {
        bookmarks.remove(postId, userId);
        return new SaveResult(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostView> saved(Long userId, int page, int size) {
        List<PostView> result = new ArrayList<>();
        for (Long postId : bookmarks.findPostIds(userId, Math.max(0, page), Math.max(1, Math.min(size, 50)))) {
            // obrisane objave se tiho preskaču
            if (posts.findById(postId).isPresent()) result.add(getPost.get(postId, userId));
        }
        return result;
    }
}
