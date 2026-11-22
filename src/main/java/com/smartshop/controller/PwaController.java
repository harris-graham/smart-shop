package com.smartshop.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Serves the web app manifest with the media type browsers expect. Spring's
 * static resource handling would otherwise fall back to
 * {@code application/octet-stream} because {@code .webmanifest} is not in the
 * default media type map, and an install prompt is then skipped.
 */
@RestController
public class PwaController {

    private static final MediaType WEB_MANIFEST =
            MediaType.parseMediaType("application/manifest+json");

    @GetMapping(value = "/manifest.webmanifest", produces = "application/manifest+json")
    public ResponseEntity<Resource> manifest() {
        return ResponseEntity.ok()
                .contentType(WEB_MANIFEST)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
                .body(new ClassPathResource("static/manifest.webmanifest"));
    }
}