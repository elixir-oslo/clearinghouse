package no.uio.ifi.clearinghouse;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.SigningKeyNotFoundException;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.Gson;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum JWKProvider {

    INSTANCE;

    private static final String KEYS = "keys";

    private Gson gson = new Gson();

    private LoadingCache<Pair<String, String>, Jwk> cache = Caffeine.newBuilder().maximumSize(100).build(this::getInternal);

    public synchronized Jwk get(String url, String keyId) {
        return cache.get(new ImmutablePair<>(url, keyId));
    }

    private Jwk getInternal(Pair<String, String> urlAndId) throws JwkException, IOException {
        var url = urlAndId.getKey();
        var keyId = urlAndId.getValue();
        return getAll(url)
                .stream()
                .filter(k -> k.getId().equals(keyId))
                .findAny()
                .orElseThrow(() -> new SigningKeyNotFoundException("No key found in " + url + " with kid " + keyId, null));
    }

    @SuppressWarnings("unchecked")
    private List<Jwk> getAll(String url) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())) {
            var content = gson.fromJson(reader, Map.class);
            var keys = (List<Map<String, Object>>) content.get(KEYS);
            return keys.stream().map(Jwk::fromValues).collect(Collectors.toList());
        }
    }

}
