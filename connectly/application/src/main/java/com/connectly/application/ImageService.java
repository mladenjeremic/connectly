package com.connectly.application;

import com.connectly.application.UseCases.*;
import com.connectly.domain.exception.NotFoundException;
import com.connectly.domain.port.ImageStorage;

public class ImageService implements LoadImage {
    private final ImageStorage storage;

    public ImageService(ImageStorage storage) { this.storage = storage; }

    @Override
    public ImageData load(String fileName) {
        if (!ImageRules.isValidStoredName(fileName)) throw new NotFoundException("Fotografija nije pronađena.");
        return new ImageData(storage.load(fileName), ImageRules.contentTypeFor(fileName));
    }
}
