package com.example.urlshortener.controller;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@RestController
public class UrlShortenerController {

    private final UrlMappingRepository repository;

    public UrlShortenerController(UrlMappingRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody String longUrl) {

        String shortCode = generateShortCode(longUrl);

       Optional<UrlMapping> existing = repository.findByShortCode(shortCode);

if (existing.isEmpty()) {
    UrlMapping mapping = new UrlMapping(shortCode, longUrl);
    repository.save(mapping);
}

return ResponseEntity.ok("http://localhost:8080/" + shortCode);
    }

    private String generateShortCode(String longUrl) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hash = md.digest(
                    longUrl.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.substring(0, 8);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/{shortCode}")
public ResponseEntity<Void> redirectToLongUrl(@PathVariable String shortCode) {

    return repository.findByShortCode(shortCode)
            .map(mapping -> ResponseEntity.status(302)
                    .header("Location", mapping.getLongUrl())
                    .<Void>build())
            .orElse(ResponseEntity.notFound().build());
}
}