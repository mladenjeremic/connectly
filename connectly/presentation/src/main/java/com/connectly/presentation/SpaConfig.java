package com.connectly.presentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Ako je zadat {@code connectly.static-dir}, backend servira build frontenda (React SPA) sa istog domena,
 * pa nema CORS-a i treba samo jedan besplatan servis. Nepoznate putanje (osim /api) vraćaju index.html.
 */
@Configuration
class SpaConfig implements WebMvcConfigurer {
    private final String staticDir;

    SpaConfig(@Value("${connectly.static-dir:}") String staticDir) { this.staticDir = staticDir; }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (staticDir == null || staticDir.isBlank()) return;
        Path root = Path.of(staticDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/**")
                .addResourceLocations(root.toUri().toString())
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        if (resourcePath.startsWith("api/") || resourcePath.startsWith("actuator/")) return null;
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) return requested;
                        return new FileSystemResource(root.resolve("index.html"));
                    }
                });
    }
}
