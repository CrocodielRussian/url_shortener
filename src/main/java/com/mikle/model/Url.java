package com.mikle.model;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
@Entity
@Table(name = "urls")
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String longUrl;

    @Column(unique = true, nullable = false)
    private String shortUrl;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
