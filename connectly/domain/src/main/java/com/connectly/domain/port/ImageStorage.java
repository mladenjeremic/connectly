package com.connectly.domain.port;

/** Skladište fotografija (lokalni folder u razvoju, kasnije objekt storage). */
public interface ImageStorage {
    /** Čuva bajtove pod generisanim bezbednim imenom i vraća to ime. */
    String save(byte[] data, String extension);
    byte[] load(String fileName);
    void delete(String fileName);
}
