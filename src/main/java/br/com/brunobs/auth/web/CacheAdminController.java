package br.com.brunobs.auth.web;


import br.com.brunobs.auth.context.CacheFactory;
import br.com.brunobs.auth.context.CacheType;
import br.com.brunobs.auth.security.AuthorizationPolicy;
import br.com.brunobs.auth.security.AuthorizationRequired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AuthorizationRequired(policy = AuthorizationPolicy.OWNER)
@RequestMapping("/admin/security/cache/{type}")
public class CacheAdminController {

    private final CacheFactory cacheFactory;

    public CacheAdminController(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    @GetMapping("/inspect")
    public Map<?, ?> inspect(@PathVariable CacheType type) {
        return cacheFactory.get(type).asMap();
    }

    @DeleteMapping("/clear")
    public void clear(@PathVariable CacheType type) {
        cacheFactory.get(type).invalidateAll();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(@PathVariable CacheType type) {

        var cache = cacheFactory.get(type);
        var stats = cache.stats();

        return Map.of(
                "hitCount", stats.hitCount(),
                "missCount", stats.missCount(),
                "evictionCount", stats.evictionCount(),
                "estimatedSize", cache.estimatedSize()
        );
    }
}