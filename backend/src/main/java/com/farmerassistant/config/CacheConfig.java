package com.farmerassistant.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("weather",
            Caffeine.newBuilder().maximumSize(200).expireAfterWrite(30, TimeUnit.MINUTES).build());
        manager.registerCustomCache("marketPrices",
            Caffeine.newBuilder().maximumSize(500).expireAfterWrite(1, TimeUnit.HOURS).build());
        manager.registerCustomCache("schemes",
            Caffeine.newBuilder().maximumSize(100).expireAfterWrite(2, TimeUnit.HOURS).build());
        manager.registerCustomCache("uvIndex",
            Caffeine.newBuilder().maximumSize(100).expireAfterWrite(1, TimeUnit.HOURS).build());
        return manager;
    }
}
