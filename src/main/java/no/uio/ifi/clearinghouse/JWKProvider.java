package no.uio.ifi.clearinghouse;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.SigningKeyNotFoundException;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Singleton class to be used for retrieving keys from JKU entry of JWT header.
 */
public enum JWKProvider {

    INSTANCE;

    private final OkHttpClient client = new OkHttpClient();

    private static final String KEYS = "keys";

    private Gson gson = new Gson();

    private LoadingCache<Pair<String, String>, Jwk> cache = Caffeine.newBuilder().maximumSize(100).build(this::getInternal);

    /**
     * Returns <code>Jwk</code> instance containing RSA Public Key with specified ID, fetched from specified URL.
     * The implementation uses cache.
     *
     * @param url   JKU URL to fetch key from.
     * @param keyId Key ID.
     * @return <code>Jwk</code> instance.
     */
    public synchronized Jwk get(String url, String keyId) {
        return cache.get(new ImmutablePair<>(url, keyId));
    }

    private Jwk getInternal(Pair<String, String> urlAndId) throws JwkException {
        var url = urlAndId.getKey();
        var keyId = urlAndId.getValue();
        return getAll(url)
                .stream()
                .filter(k -> k.getId().equals(keyId))
                .findAny()
                .orElseThrow(() -> new SigningKeyNotFoundException("No key found in " + url + " with kid " + keyId, null));
    }

    @SuppressWarnings("unchecked")
    private List<Jwk> getAll(String url) {
        // JSONObject body = Unirest.get(url).asJson().getBody().getObject();
        // JSONArray keysArray = body.getJSONArray(KEYS);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        JsonArray keysArray;
        try {
            ResponseBody body = client.newCall(request).execute().body();
            keysArray = gson.fromJson(body.string(), JsonObject.class).getAsJsonArray(KEYS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return (List<Jwk>) keysArray
                .asList()
                .stream()
                .map(k -> gson.fromJson(k.toString(), Map.class))
                .map(k -> Jwk.fromValues((Map<String, Object>) k))
                .collect(Collectors.toList());
    }

}
