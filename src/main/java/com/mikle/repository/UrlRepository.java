package com.mikle.repository;

import com.mikle.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByUserIdAndLongUrl(Long userId, String longUrl);

    @Query("select u.longUrl from Url u where u.shortUrl = :shortUrl")
    Optional<String> findLongUrlByShortUrl(@Param("shortUrl") String shortUrl);

    @Query("SELECT u.shortUrl FROM Url u")
    List<String> findAllShortUrls();

    @Query("""
           select u.shortUrl
           from Url u
           where u.user.id = :userId and u.longUrl = :longUrl
           """)
    Optional<String> findShortUrlByUserIdAndLongUrl(
            @Param("userId") Long userId,
            @Param("longUrl") String longUrl
    );

    List<Url> findByUserId(Long userId);

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
}