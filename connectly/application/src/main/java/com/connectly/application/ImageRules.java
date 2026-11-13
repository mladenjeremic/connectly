package com.connectly.application;

import com.connectly.application.UseCases.ImageUpload;
import com.connectly.domain.exception.DomainException;
import java.util.Locale;

/** Provera fotografija: tip se utvrđuje po sadržaju (magic bytes), a ne po imenu fajla ili Content-Type-u. */
public final class ImageRules {
    private ImageRules() {}

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    public static final int MAX_IMAGES_PER_POST = 10;
    public static final String URL_PREFIX = "/api/images/";

    public record Detected(String extension, String contentType) {}

    public static Detected inspect(ImageUpload upload) {
        if (upload == null || upload.data() == null || upload.data().length == 0)
            throw new DomainException("Fajl je prazan.");
        byte[] b = upload.data();
        if (b.length > MAX_BYTES)
            throw new DomainException("Fotografija je prevelika (najviše 5 MB).");
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF)
            return new Detected("jpg", "image/jpeg");
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A)
            return new Detected("png", "image/png");
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P')
            return new Detected("webp", "image/webp");
        throw new DomainException("Dozvoljeni su samo JPEG, PNG i WebP formati.");
    }

    public static String url(String fileName) {
        return fileName == null || fileName.isBlank() ? null : URL_PREFIX + fileName;
    }

    /** Sačuvana imena su UUID + ekstenzija; sve drugo se odbacuje (zaštita od path traversal-a). */
    public static boolean isValidStoredName(String fileName) {
        return fileName != null && fileName.matches("^[a-f0-9\\-]{36}\\.(jpg|png|webp)$");
    }

    public static String contentTypeFor(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
