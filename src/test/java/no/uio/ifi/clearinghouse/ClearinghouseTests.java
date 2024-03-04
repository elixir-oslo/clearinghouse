package no.uio.ifi.clearinghouse;

import lombok.SneakyThrows;
import no.uio.ifi.clearinghouse.model.ByValue;
import no.uio.ifi.clearinghouse.model.Visa;
import no.uio.ifi.clearinghouse.model.VisaType;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.Optional;


public class ClearinghouseTests {

    private MockWebServer mockWebServer;
    private CredentialsProvider credentialsProvider;
    private String publicKey;
    private String accessToken;
    private String visaToken;

    private HttpUrl userInfoEndpoint;
    private HttpUrl oidcConfigEndpoint;

    @SneakyThrows
    @BeforeEach
    public void init() {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        HttpUrl jwkEndPoint = mockWebServer.url("/jwk");
        userInfoEndpoint = mockWebServer.url("/userinfo");
        oidcConfigEndpoint = mockWebServer.url("/config");

        // generate credentials
        credentialsProvider = new CredentialsProvider(baseUrl);
        accessToken = credentialsProvider.getAccessToken();
        visaToken = credentialsProvider.getVisaToken();
        String jwk = Files.readString(Path.of("src/test/resources/jwk.json"));
        String passport = credentialsProvider.getPassportJsonString();
        publicKey = Files.readString(Path.of("src/test/resources/public.pem"));
        String config = Files.readString(Path.of("src/test/resources/oidcConfig.json"))
                .replace("https://login.elixir-czech.org/oidc/jwk", jwkEndPoint.toString());

        // Mock-webserver to create custom responses
        MockResponse passportResponse = new MockResponse().setResponseCode(200).setBody(passport);
        mockWebServer.enqueue(passportResponse);
        MockResponse jwkResponse = new MockResponse().setResponseCode(200).setBody(jwk);
        mockWebServer.enqueue(jwkResponse);
        MockResponse configResponse = new MockResponse().setResponseCode(200).setBody(config);
        mockWebServer.enqueue(configResponse);

        // Map response to end-points
        Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                assert request.getPath() != null;
                return switch (request.getPath()) {
                    case "/userinfo" -> passportResponse;
                    case "/jwk" -> jwkResponse;
                    case "/config" -> configResponse;
                    default -> new MockResponse().setResponseCode(404);
                };
            }
        };
        mockWebServer.setDispatcher(dispatcher);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }


    @SneakyThrows
    @Test
    public void getVisasTest() {
        Collection<Visa> visas = Clearinghouse.INSTANCE.getVisas(accessToken, oidcConfigEndpoint.toString());
        Assertions.assertEquals(1, visas.size());
        Visa visa = visas.iterator().next();
        Assertions.assertEquals("test@elixir-europe.org", visa.getSub());
        Assertions.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
        Assertions.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
        Assertions.assertEquals("affiliate@google.com", visa.getValue());
        Assertions.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
        Assertions.assertNull(visa.getConditions());
        Assertions.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
    }

    @SneakyThrows
    @Test
    public void getVisasWithPEMPublicKeyTestTest() {
        Collection<Visa> visas = Clearinghouse.INSTANCE.getVisasWithPEMPublicKey(accessToken, publicKey);
        Assertions.assertEquals(1, visas.size());
        Visa visa = visas.iterator().next();
        Assertions.assertEquals("test@elixir-europe.org", visa.getSub());
        Assertions.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
        Assertions.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
        Assertions.assertEquals("affiliate@google.com", visa.getValue());
        Assertions.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
        Assertions.assertNull(visa.getConditions());
        Assertions.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
    }

    @SneakyThrows
    @Test
    public void getVisaTest() {
        Optional<Visa> optionalVisa = Clearinghouse.INSTANCE.getVisa(visaToken);
        Assertions.assertTrue(optionalVisa.isPresent());
        Visa visa = optionalVisa.get();
        Assertions.assertEquals("test@elixir-europe.org", visa.getSub());
        Assertions.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
        Assertions.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
        Assertions.assertEquals("affiliate@google.com", visa.getValue());
        Assertions.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
        Assertions.assertNull(visa.getConditions());
        Assertions.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
    }

    @SneakyThrows
    @Test
    public void getVisaWithPEMPublicKeyTest() {
        Optional<Visa> optionalVisa = Clearinghouse.INSTANCE.getVisaWithPEMPublicKey(visaToken, publicKey);
        Assertions.assertTrue(optionalVisa.isPresent());
        Visa visa = optionalVisa.get();
        Assertions.assertEquals("test@elixir-europe.org", visa.getSub());
        Assertions.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
        Assertions.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
        Assertions.assertEquals("affiliate@google.com", visa.getValue());
        Assertions.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
        Assertions.assertNull(visa.getConditions());
        Assertions.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
    }

    @SneakyThrows
    @Test
    public void getVisaTokensTest() {
        Collection<String> visaTokens = Clearinghouse.INSTANCE.getVisaTokens(accessToken, oidcConfigEndpoint.toString());
        Assertions.assertEquals(1, visaTokens.size());
        Assertions.assertEquals(visaToken, visaTokens.iterator().next());
    }

    @SneakyThrows
    @Test
    public void getVisaTokensWithPEMPublicKeyTest() {
        Collection<String> visaTokens = Clearinghouse.INSTANCE.getVisaTokensWithPEMPublicKey(accessToken, publicKey);
        Assertions.assertEquals(1, visaTokens.size());
        Assertions.assertEquals(visaToken, visaTokens.iterator().next());
    }

    @SneakyThrows
    @Test
    public void getVisaTokensFromOpaqueTokenTest() {
        Collection<String> visaTokens = Clearinghouse.INSTANCE.getVisaTokensFromOpaqueToken(accessToken, userInfoEndpoint.toString());
        Assertions.assertEquals(1, visaTokens.size());
        Assertions.assertEquals(visaToken, visaTokens.iterator().next());
    }

    @Test
    public void getVisaTokensWithPublicKeyTest() {
        RSAPublicKey publicKey = (RSAPublicKey) credentialsProvider.getPublicKey();
        var visaTokens = Clearinghouse.INSTANCE.getVisaTokensWithPublicKey(accessToken, publicKey);
        Assertions.assertEquals(visaToken, visaTokens.iterator().next());
    }

    @Test
    public void getVisaWithPublicKeyTest() {
        RSAPublicKey publicKey = (RSAPublicKey) credentialsProvider.getPublicKey();
        var optionalVisa = Clearinghouse.INSTANCE.getVisaWithPublicKey(visaToken, publicKey);
        Assertions.assertTrue(optionalVisa.isPresent());
        Visa visa = optionalVisa.get();
        Assertions.assertEquals("test@elixir-europe.org", visa.getSub());
        Assertions.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
        Assertions.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
        Assertions.assertEquals("affiliate@google.com", visa.getValue());
        Assertions.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
        Assertions.assertNull(visa.getConditions());
        Assertions.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
    }
}
