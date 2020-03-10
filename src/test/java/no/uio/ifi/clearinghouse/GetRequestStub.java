package no.uio.ifi.clearinghouse;

import kong.unirest.*;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class GetRequestStub implements GetRequest {

    private String json;

    public GetRequestStub(String json) {
        this.json = json;
    }

    @Override
    public GetRequest routeParam(String name, String value) {
        return null;
    }

    @Override
    public GetRequest routeParam(Map<String, Object> params) {
        return null;
    }

    @Override
    public GetRequest basicAuth(String username, String password) {
        return null;
    }

    @Override
    public GetRequest accept(String value) {
        return null;
    }

    @Override
    public GetRequest responseEncoding(String encoding) {
        return null;
    }

    @Override
    public GetRequest header(String name, String value) {
        return this;
    }

    @Override
    public GetRequest headerReplace(String name, String value) {
        return null;
    }

    @Override
    public GetRequest headers(Map<String, String> headerMap) {
        return null;
    }

    @Override
    public GetRequest cookie(String name, String value) {
        return null;
    }

    @Override
    public GetRequest cookie(Cookie cookie) {
        return null;
    }

    @Override
    public GetRequest queryString(String name, Object value) {
        return null;
    }

    @Override
    public GetRequest queryString(String name, Collection<?> value) {
        return null;
    }

    @Override
    public GetRequest queryString(Map<String, Object> parameters) {
        return null;
    }

    @Override
    public GetRequest withObjectMapper(ObjectMapper mapper) {
        return null;
    }

    @Override
    public GetRequest socketTimeout(int millies) {
        return null;
    }

    @Override
    public GetRequest connectTimeout(int millies) {
        return null;
    }

    @Override
    public GetRequest proxy(String host, int port) {
        return null;
    }

    @Override
    public GetRequest downloadMonitor(ProgressMonitor monitor) {
        return null;
    }

    @Override
    public HttpResponse<String> asString() {
        return null;
    }

    @Override
    public CompletableFuture<HttpResponse<String>> asStringAsync() {
        return null;
    }

    @Override
    public CompletableFuture<HttpResponse<String>> asStringAsync(Callback<String> callback) {
        return null;
    }

    @Override
    public HttpResponse<byte[]> asBytes() {
        return null;
    }

    @Override
    public CompletableFuture<HttpResponse<byte[]>> asBytesAsync() {
        return null;
    }

    @Override
    public CompletableFuture<HttpResponse<byte[]>> asBytesAsync(Callback<byte[]> callback) {
        return null;
    }

    @Override
    public HttpResponse<JsonNode> asJson() {
        return new HttpResponseStub(json);
    }

    @Override
    public CompletableFuture<HttpResponse<JsonNode>> asJsonAsync() {
        return null;
    }

    @Override
    public CompletableFuture<HttpResponse<JsonNode>> asJsonAsync(Callback<JsonNode> callback) {
        return null;
    }

    @Override
    public <T> HttpResponse<T> asObject(Class<? extends T> responseClass) {
        return null;
    }

    @Override
    public <T> HttpResponse<T> asObject(GenericType<T> genericType) {
        return null;
    }

    @Override
    public <T> HttpResponse<T> asObject(Function<RawResponse, T> function) {
        return null;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> asObjectAsync(Class<? extends T> responseClass) {
        return null;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> asObjectAsync(Class<? extends T> responseClass, Callback<T> callback) {
        return null;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> asObjectAsync(GenericType<T> genericType) {
        return null;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> asObjectAsync(GenericType<T> genericType, Callback<T> callback) {
        return null;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> asObjectAsync(Function<RawResponse, T> function) {
        return null;
    }

    @Override
    public HttpResponse<File> asFile(String path) {
        return null;
    }

    @Override
    public CompletableFuture<HttpResponse<File>> asFileAsync(String path) {
        return null;
    }

    @Override
    public CompletableFuture<HttpResponse<File>> asFileAsync(String path, Callback<File> callback) {
        return null;
    }

    @Override
    public <T> PagedList<T> asPaged(Function<HttpRequest, HttpResponse> mappingFunction, Function<HttpResponse<T>, String> linkExtractor) {
        return null;
    }

    @Override
    public HttpResponse asEmpty() {
        return null;
    }

    @Override
    public CompletableFuture<HttpResponse<Empty>> asEmptyAsync() {
        return null;
    }

    @Override
    public CompletableFuture<HttpResponse<Empty>> asEmptyAsync(Callback<Empty> callback) {
        return null;
    }

    @Override
    public void thenConsume(Consumer<RawResponse> consumer) {

    }

    @Override
    public void thenConsumeAsync(Consumer<RawResponse> consumer) {

    }

    @Override
    public HttpMethod getHttpMethod() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public Headers getHeaders() {
        return null;
    }

    @Override
    public int getSocketTimeout() {
        return 0;
    }

    @Override
    public int getConnectTimeout() {
        return 0;
    }

    @Override
    public Proxy getProxy() {
        return null;
    }

    @Override
    public HttpRequestSummary toSummary() {
        return null;
    }

}
