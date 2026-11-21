package br.com.portalmanager.platform.authorization.entrypoint.http;

import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationPolicy;
import br.com.portalmanager.platform.authorization.core.authorization.AuthorizationRequired;
import br.com.portalmanager.platform.library.redis.api.store.RedisKeyPage;
import br.com.portalmanager.platform.library.redis.api.store.RedisStore;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@AuthorizationRequired(policy = AuthorizationPolicy.OWNER)
@RequestMapping("/admin/security/cache/{type}")
public class CacheAdminController {
    private static final Map<String, String> PREFIXES = Map.of(
            "AUTHZ", "AUTH::",
            "JWT", "authorization:session:",
            "REPAIR_AUTHZ", "authorization:repair:"
    );
    private final RedisStore redis;

    public CacheAdminController(RedisStore redis) {
        this.redis = redis;
    }

    @GetMapping("/inspect")
    public List<String> inspect(@PathVariable String type) {
        return matchingKeys(prefix(type));
    }

    @DeleteMapping("/clear")
    public Map<String, Long> clear(@PathVariable String type) {
        List<String> keys = matchingKeys(prefix(type));
        long deleted = keys.isEmpty() ? 0 : redis.deleteAll(keys);
        return Map.of("deleted", deleted);
    }

    @GetMapping("/stats")
    public Map<String, Integer> stats(@PathVariable String type) {
        return Map.of("estimatedSize", matchingKeys(prefix(type)).size());
    }

    private List<String> matchingKeys(String prefix) {
        List<String> keys = new ArrayList<>();
        String cursor = "0";
        do {
            RedisKeyPage page = redis.scan(prefix, cursor, 250);
            keys.addAll(page.keys());
            cursor = page.cursor();
        } while (!"0".equals(cursor));
        return List.copyOf(keys);
    }

    private String prefix(String type) {
        String prefix = PREFIXES.get(type.toUpperCase());
        if (prefix == null) throw new IllegalArgumentException("Unknown cache type");
        return prefix;
    }
}
