package com.campus.smart_campus.common.web;

import java.nio.charset.StandardCharsets;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.TimeUnit;

@RestController
public class FaviconController {

    private static final byte[] FAVICON_BYTES = (
            "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 64 64'>" +
            "<defs><linearGradient id='g' x1='0' y1='0' x2='1' y2='1'>" +
            "<stop offset='0%' stop-color='#111827'/><stop offset='100%' stop-color='#4b5563'/>" +
            "</linearGradient></defs>" +
            "<rect width='64' height='64' rx='16' fill='url(#g)'/>" +
            "<path d='M16 24h32v4H16zm0 8h32v4H16zm0 8h20v4H16z' fill='#f8fafc'/>" +
            "<circle cx='46' cy='44' r='6' fill='#f59e0b'/>" +
            "</svg>"
    ).getBytes(StandardCharsets.UTF_8);

    @GetMapping(value = "/favicon.ico", produces = "image/svg+xml")
    public ResponseEntity<byte[]> favicon() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic().getHeaderValue())
                .body(FAVICON_BYTES);
    }
}
