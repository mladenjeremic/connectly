package com.connectly.application;

import com.connectly.application.UseCases.ImageUpload;
import com.connectly.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ImageRulesTest {
    private static ImageUpload up(int... bytes) {
        byte[] b = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) b[i] = (byte) bytes[i];
        return new ImageUpload(b, "whatever/ignored");
    }

    @Test void detectsJpeg() { assertEquals("jpg", ImageRules.inspect(up(0xFF, 0xD8, 0xFF, 0xE0)).extension()); }
    @Test void detectsPng() { assertEquals("png", ImageRules.inspect(up(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)).extension()); }
    @Test void detectsWebp() {
        assertEquals("webp", ImageRules.inspect(up('R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P')).extension());
    }
    @Test void rejectsExecutableDisguisedAsImage() {
        assertThrows(DomainException.class, () -> ImageRules.inspect(up('M', 'Z', 0x90, 0x00)));
    }
    @Test void rejectsEmptyFile() { assertThrows(DomainException.class, () -> ImageRules.inspect(new ImageUpload(new byte[0], "image/png"))); }
    @Test void rejectsTooLargeFile() {
        byte[] big = new byte[(int) ImageRules.MAX_BYTES + 1];
        big[0] = (byte) 0xFF; big[1] = (byte) 0xD8; big[2] = (byte) 0xFF;
        assertThrows(DomainException.class, () -> ImageRules.inspect(new ImageUpload(big, "image/jpeg")));
    }
    @Test void storedNamesMustBeUuidWithKnownExtension() {
        assertTrue(ImageRules.isValidStoredName("3f2a9c1e-1111-4222-8333-444455556666.png"));
        assertFalse(ImageRules.isValidStoredName("../../etc/passwd"));
        assertFalse(ImageRules.isValidStoredName("3f2a9c1e-1111-4222-8333-444455556666.exe"));
    }
}
