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

    @Transactional
    public Url addUrl(String longUrl) {
        User user = getCurrentUser();

        System.out.println("Principal userId: " + user);
        Url newUrl = new Url();

        String shortUrl = shortnerService.generateShortUrl(longUrl);

        newUrl.setUser(user);
        newUrl.setLongUrl(longUrl);
        newUrl.setShortUrl(shortUrl);

        return urlRepository.save(newUrl);
    }

    @Transactional
    public void deleteUrl(Long urlId) {
        urlRepository.deleteById(urlId);
    }

    @Transactional
    public void clearUrls() {
        User user = getCurrentUser();
        urlRepository.deleteByUserId(user.getId());
    }

    public Optional<Url> findByShortUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl);
    }

    public List<Url> getUserUrls() {
        User user = getCurrentUser();
        return urlRepository.findByUserId(user.getId());
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
