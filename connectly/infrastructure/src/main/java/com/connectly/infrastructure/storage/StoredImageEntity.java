package com.connectly.infrastructure.storage;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "stored_images")
public class StoredImageEntity {
    @Id @Column(length = 100) public String fileName;
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false, length = 8_000_000) public byte[] data;
    public Instant createdAt = Instant.now();
}
