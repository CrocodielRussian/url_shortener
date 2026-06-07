package com.mikle.service;

import com.mikle.model.Url;
import com.mikle.model.User;
import com.mikle.repository.UrlRepository;
import com.mikle.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortnerService shortnerService;
    private final UrlCacheService urlCacheService;

    @Transactional
    public Url addUrl(String longUrl) {
        User user = getCurrentUser();
        Long userId = user.getId();

        String cachedShortUrl =
                urlCacheService.findShortUrlByUserIdAndLongUrl(userId, longUrl);

        if (cachedShortUrl != null) {
            return urlRepository.findByUserIdAndLongUrl(userId, longUrl)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        Optional<Url> existingUrl =
                urlRepository.findByUserIdAndLongUrl(userId, longUrl);

        if (existingUrl.isPresent()) {
            Url url = existingUrl.get();

            urlCacheService.putUserLongToShort(
                    userId,
                    url.getLongUrl(),
                    url.getShortUrl()
            );

            urlCacheService.putShortToLong(
                    url.getShortUrl(),
                    url.getLongUrl()
            );

            return url;
        }

        Url newUrl = new Url();

        String shortUrl = shortnerService.generateShortUrl(longUrl);

        newUrl.setUser(user);
        newUrl.setLongUrl(longUrl);
        newUrl.setShortUrl(shortUrl);

        Url savedUrl = urlRepository.save(newUrl);

        urlCacheService.putUserLongToShort(
                userId,
                savedUrl.getLongUrl(),
                savedUrl.getShortUrl()
        );

        urlCacheService.putShortToLong(
                savedUrl.getShortUrl(),
                savedUrl.getLongUrl()
        );

        return savedUrl;
    }

    @Transactional(readOnly = true)
    public String resolveLongUrl(String shortUrl) {
        String longUrl = urlCacheService.findLongUrlByShortUrl(shortUrl);

        if (longUrl == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return longUrl;
    }

    @Transactional
    public void deleteUrl(Long urlId) {
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Long userId = url.getUser().getId();
        String longUrl = url.getLongUrl();
        String shortUrl = url.getShortUrl();

        urlRepository.delete(url);

        urlCacheService.evictUserLongToShort(userId, longUrl);
        urlCacheService.evictShortToLong(shortUrl);
    }

    @Transactional
    public void clearUrls() {
        User user = getCurrentUser();
        List<Url> urls = urlRepository.findByUserId(user.getId());

        for (Url url : urls) {
            urlCacheService.evictUserLongToShort(
                    user.getId(),
                    url.getLongUrl()
            );

            urlCacheService.evictShortToLong(url.getShortUrl());
        }

        urlRepository.deleteByUserId(user.getId());
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getUser();
        }

        throw new RuntimeException("Unknown principal: " + principal.getClass().getName());
    }
}