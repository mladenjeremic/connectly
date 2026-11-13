package com.connectly.presentation;

import com.connectly.application.UseCases.ImageUpload;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Pretvara multipart fajlove u DTO aplikacionog sloja. Prazni delovi forme se ignorišu. */
final class Uploads {
    private Uploads() {}

    static ImageUpload one(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        return new ImageUpload(file.getBytes(), file.getContentType());
    }

    static List<ImageUpload> many(List<MultipartFile> files) throws IOException {
        List<ImageUpload> result = new ArrayList<>();
        if (files == null) return result;
        for (MultipartFile f : files) {
            ImageUpload up = one(f);
            if (up != null) result.add(up);
        }
        return result;
    }
}
