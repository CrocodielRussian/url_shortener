package com.mikle.service;

import com.mikle.repository.UrlRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class UrlService {
    private final UrlRepository repo;

    public UrlService(UrlRepository repo) {
        this.repo = repo;
    }
}
