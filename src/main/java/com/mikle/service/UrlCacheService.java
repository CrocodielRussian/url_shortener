package com.mikle.service;

import com.mikle.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlCacheService {

    private final UrlRepository urlRepository;

    @Cacheable(
            cacheNames = "shortToLong",
            key = "#shortUrl",
            unless = "#result == null",
            sync = true
    )
    public String findLongUrlByShortUrl(String shortUrl) {
        return urlRepository.findLongUrlByShortUrl(shortUrl).orElse(null);
    }

    @Cacheable(
            cacheNames = "userLongToShort",
            key = "#userId + ':' + #longUrl",
            unless = "#result == null",
            sync = true
    )
    public String findShortUrlByUserIdAndLongUrl(Long userId, String longUrl) {
        return urlRepository.findShortUrlByUserIdAndLongUrl(userId, longUrl)
                .orElse(null);
    }

    @CachePut(cacheNames = "shortToLong", key = "#shortUrl")
    public String putShortToLong(String shortUrl, String longUrl) {
        return longUrl;
    }

    @CachePut(cacheNames = "userLongToShort", key = "#userId + ':' + #longUrl")
    public String putUserLongToShort(Long userId, String longUrl, String shortUrl) {
        return shortUrl;
    }

    @CacheEvict(cacheNames = "shortToLong", key = "#shortUrl")
    public void evictShortToLong(String shortUrl) {
    }

    @CacheEvict(cacheNames = "userLongToShort", key = "#userId + ':' + #longUrl")
    public void evictUserLongToShort(Long userId, String longUrl) {
    }
}