package com.mikle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlResponse {
    private Long   id;
    private String shortUrl;
    private String originalUrl;
}
