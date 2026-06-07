package com.mikle.model;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
@Entity
@Table(
        name = "urls",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_url_short", columnNames = "short_url"),
                @UniqueConstraint(name = "uk_user_long_url", columnNames = {"user_id", "long_url"})
        }
)
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_url", unique = true, nullable = false)
    private String shortUrl;

    @Column(name = "long_url", nullable = false, length = 2048)
    private String longUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}