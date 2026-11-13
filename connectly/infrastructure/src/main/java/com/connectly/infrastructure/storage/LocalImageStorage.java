package com.connectly.infrastructure.storage;

import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.port.ImageStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/** Čuva fotografije u lokalnom folderu. Ime fajla je uvek generisan UUID, nikad originalni naziv. */
@Component
@ConditionalOnProperty(name = "connectly.storage.type", havingValue = "local", matchIfMissing = true)
class LocalImageStorage implements ImageStorage {
    private final Path root;

    LocalImageStorage(@Value("${connectly.upload.dir:uploads}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String save(byte[] data, String extension) {
        String fileName = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(root);
            Files.write(resolve(fileName), data);
        } catch (IOException e) {
            throw new UncheckedIOException("Ne mogu da sačuvam fotografiju", e);
        }
        return fileName;
    }

    @Override
    public byte[] load(String fileName) {
        try {
            return Files.readAllBytes(resolve(fileName));
        } catch (NoSuchFileException e) {
            throw new NotFoundException("Fotografija nije pronađena.");
        } catch (IOException e) {
            throw new UncheckedIOException("Ne mogu da pročitam fotografiju", e);
        }
    }

    @Override
    public void delete(String fileName) {
        try {
            Files.deleteIfExists(resolve(fileName));
        } catch (IOException e) {
            // brisanje fajla nije kritično; zaostali fajl ne pravi štetu
        }
    }

    /** Sprečava izlazak iz upload foldera (path traversal). */
    private Path resolve(String fileName) {
        Path path = root.resolve(fileName).normalize();
        if (!path.startsWith(root)) throw new IllegalArgumentException("Neispravno ime fajla");
        return path;
    }
}
