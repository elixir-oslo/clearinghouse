package no.uio.ifi.clearinghouse;

import kong.unirest.*;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class HttpResponseStub implements HttpResponse<JsonNode> {

    private final String json;

    public HttpResponseStub(String json) {
        this.json = json;
    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getStatusText() {
        return null;
    }

    @Override
    public Headers getHeaders() {
        return null;
    }

    @Override
    public JsonNode getBody() {
        return new JsonNode(json);
    }

    @Override
    public Optional<UnirestParsingException> getParsingError() {
        return Optional.empty();
    }

    @Override
    public <V> V mapBody(Function<JsonNode, V> func) {
        return null;
    }

    @Override
    public <V> HttpResponse<V> map(Function<JsonNode, V> func) {
        return null;
    }

    @Override
    public HttpResponse<JsonNode> ifSuccess(Consumer<HttpResponse<JsonNode>> consumer) {
        return null;
    }

    @Override
    public HttpResponse<JsonNode> ifFailure(Consumer<HttpResponse<JsonNode>> consumer) {
        return null;
    }

    @Override
    public <E> HttpResponse<JsonNode> ifFailure(Class<? extends E> errorClass, Consumer<HttpResponse<E>> consumer) {
        return null;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public <E> E mapError(Class<? extends E> errorClass) {
        return null;
    }

    @Override
    public Cookies getCookies() {
        return null;
    }

    @Override
    public HttpRequestSummary getRequestSummary() { return null; }

}
