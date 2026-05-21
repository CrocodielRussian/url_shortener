package com.mikle.controller;

import com.mikle.dto.UrlRequest;
import com.mikle.dto.UrlResponse;
import com.mikle.model.Url;
import com.mikle.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/url")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/add")
    public ResponseEntity<UrlResponse> add(@RequestBody UrlRequest request) {
        Url url = urlService.addUrl(request.getUrl());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(url));
    }

    @GetMapping("/my")
    public ResponseEntity<List<UrlResponse>> getUrls() {
        List<UrlResponse> urls = urlService.getUserUrls()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(urls);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        urlService.deleteUrl(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearUrls() {
        urlService.clearUrls();
        return ResponseEntity.ok().build();
    }

    private UrlResponse toResponse(Url url) {
        return new UrlResponse(
                url.getId(),
                "http://localhost/" + url.getShortUrl(),
                url.getLongUrl()
        );
    }
}