package com.connectly.infrastructure.storage;

import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.port.ImageStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Slike u bazi — za besplatan hosting bez trajnog diska (Render, Koyeb free...).
 * Uključuje se sa {@code connectly.storage.type=database}. Za veći obim pređi na S3/R2.
 */
@Component
@ConditionalOnProperty(name = "connectly.storage.type", havingValue = "database")
class DatabaseImageStorage implements ImageStorage {
    private final StoredImageJpaRepository repo;
    DatabaseImageStorage(StoredImageJpaRepository repo) { this.repo = repo; }

    @Override
    public String save(byte[] data, String extension) {
        String ext = extension == null ? "" : extension.replaceAll("[^a-zA-Z0-9]", "");
        String name = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext.toLowerCase());
        StoredImageEntity e = new StoredImageEntity();
        e.fileName = name; e.data = data;
        repo.save(e);
        return name;
    }

    @Override
    public byte[] load(String fileName) {
        return repo.findById(fileName).map(e -> e.data)
                .orElseThrow(() -> new NotFoundException("Fotografija nije pronađena."));
    }

    @Override
    public void delete(String fileName) {
        if (repo.existsById(fileName)) repo.deleteById(fileName);
    }
}
