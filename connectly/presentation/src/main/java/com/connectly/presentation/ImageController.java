package com.connectly.presentation;

import com.connectly.application.UseCases.ImageData;
import com.connectly.application.UseCases.LoadImage;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Duration;

/** Javno servira fotografije. Imena fajlova su nepogađajući UUID-evi, pa <img> tag radi bez tokena. */
@RestController
@RequestMapping("/api/images")
class ImageController {
    private final LoadImage loadImage;
    ImageController(LoadImage loadImage) { this.loadImage = loadImage; }

    @GetMapping("/{fileName}")
    ResponseEntity<byte[]> get(@PathVariable String fileName) {
        ImageData image = loadImage.load(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .body(image.bytes());
    }
}
