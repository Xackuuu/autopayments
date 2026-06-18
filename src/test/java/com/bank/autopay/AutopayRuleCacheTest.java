package com.bank.autopay;


import com.bank.autopay.executor.AutoPayService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AutopayRuleCacheTest {

    @Autowired
    private AutoPayService service;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @Transactional
    void shouldCacheActiveRules() {
        log.info(cacheStats());
        service.getActiveRules();
        log.info(cacheStats());
        service.getActiveRules();
        log.info(cacheStats());
    }

    private String cacheStats() {
        Cache cache = cacheManager.getCache("activeRules");
        if (cache instanceof CaffeineCache caffeineCache) {
            return "\n✅✅✅" + caffeineCache.getNativeCache().stats() + "✅✅✅";
        }
        return "\n❌❌❌Cache is null❌❌❌";
    }
}
