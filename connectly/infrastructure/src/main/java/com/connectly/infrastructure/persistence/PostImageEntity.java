package com.connectly.infrastructure.persistence;

import com.connectly.domain.model.PostImage;
import jakarta.persistence.*;

@Entity @Table(name = "post_images", indexes = @Index(columnList = "postId"))
public class PostImageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long postId;
    @Column(nullable = false, length = 100) public String fileName;
    @Column(nullable = false, length = 50) public String contentType;
    public long fileSize;
    public int sortOrder;

    static PostImageEntity from(PostImage i) {
        PostImageEntity e = new PostImageEntity();
        e.id = i.id(); e.postId = i.postId(); e.fileName = i.fileName(); e.contentType = i.contentType();
        e.fileSize = i.fileSize(); e.sortOrder = i.sortOrder();
        return e;
    }

    PostImage toDomain() { return new PostImage(id, postId, fileName, contentType, fileSize, sortOrder); }
}
