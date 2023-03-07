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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.Optional;

@RunWith(JUnit4.class)
public class ClearinghouseTests {

    private MockWebServer mockWebServer;
    private CredentialsProvider credentialsProvider;
    private String publicKey;
    private String accessToken;
    private String visaToken;

    private HttpUrl userInfoEndpoint;
    private HttpUrl oidcConfigEndpoint;

    @SneakyThrows
    @Before
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

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }


    @SneakyThrows
    @Test
    public void getVisasTest() {
        Collection<Visa> visas = Clearinghouse.INSTANCE.getVisas(accessToken, oidcConfigEndpoint.toString());
        Assert.assertEquals(1, visas.size());
        Visa visa = visas.iterator().next();
        Assert.assertEquals("test@elixir-europe.org", visa.getSub());
        Assert.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
        Assert.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
        Assert.assertEquals("affiliate@google.com", visa.getValue());
        Assert.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
        Assert.assertNull(visa.getConditions());
        Assert.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());

    }

    @SneakyThrows
    @Test
    public void getVisasWithPEMPublicKeyTestTest() {
        Collection<Visa> visas = Clearinghouse.INSTANCE.getVisasWithPEMPublicKey(accessToken, publicKey);
        Assert.assertEquals(1, visas.size());
        Visa visa = visas.iterator().next();
        Assert.assertEquals("test@elixir-europe.org", visa.getSub());
        Assert.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
        Assert.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
        Assert.assertEquals("affiliate@google.com", visa.getValue());
        Assert.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
        Assert.assertNull(visa.getConditions());
        Assert.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
    }

    @SneakyThrows
    @Test
    public void getVisaTest() {
        Optional<Visa> optionalVisa = Clearinghouse.INSTANCE.getVisa(visaToken);
        Assert.assertTrue(optionalVisa.isPresent());
        Visa visa = optionalVisa.get();
        Assert.assertEquals("test@elixir-europe.org", visa.getSub());
        Assert.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
        Assert.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
        Assert.assertEquals("affiliate@google.com", visa.getValue());
        Assert.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
        Assert.assertNull(visa.getConditions());
        Assert.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
    }

    @SneakyThrows
    @Test
    public void getVisaWithPEMPublicKeyTest() {
        Optional<Visa> optionalVisa = Clearinghouse.INSTANCE.getVisaWithPEMPublicKey(visaToken, publicKey);
        Assert.assertTrue(optionalVisa.isPresent());
        Visa visa = optionalVisa.get();
        Assert.assertEquals("test@elixir-europe.org", visa.getSub());
        Assert.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
        Assert.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
        Assert.assertEquals("affiliate@google.com", visa.getValue());
        Assert.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
        Assert.assertNull(visa.getConditions());
        Assert.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
    }

    @SneakyThrows
    @Test
    public void getVisaTokensTest() {
        Collection<String> visaTokens = Clearinghouse.INSTANCE.getVisaTokens(accessToken, oidcConfigEndpoint.toString());
        Assert.assertEquals(1, visaTokens.size());
        Assert.assertEquals(visaToken, visaTokens.iterator().next() + "\n");
    }

    @SneakyThrows
    @Test
    public void getVisaTokensWithPEMPublicKeyTest() {
        Collection<String> visaTokens = Clearinghouse.INSTANCE.getVisaTokensWithPEMPublicKey(accessToken, publicKey);
        Assert.assertEquals(1, visaTokens.size());
        Assert.assertEquals(visaToken, visaTokens.iterator().next() + "\n");
    }

    @SneakyThrows
    @Test
    public void getVisaTokensFromOpaqueTokenTest() {
        Collection<String> visaTokens = Clearinghouse.INSTANCE.getVisaTokensFromOpaqueToken(accessToken, userInfoEndpoint.toString());
        Assert.assertEquals(1, visaTokens.size());
        Assert.assertEquals(visaToken, visaTokens.iterator().next() + "\n");
    }

    @Test
    public void getVisaTokensWithPublicKeyTest() {
        RSAPublicKey publicKey = (RSAPublicKey) credentialsProvider.getPublicKey();
        var visaTokens = Clearinghouse.INSTANCE.getVisaTokensWithPublicKey(accessToken, publicKey);
        Assert.assertEquals(visaToken, visaTokens.iterator().next() + "\n");
    }

    @Test
    public void getVisaWithPublicKeyTest() {
        RSAPublicKey publicKey = (RSAPublicKey) credentialsProvider.getPublicKey();
        var optionalVisa = Clearinghouse.INSTANCE.getVisaWithPublicKey(visaToken, publicKey);
        Assert.assertTrue(optionalVisa.isPresent());
        Visa visa = optionalVisa.get();
        Assert.assertEquals("test@elixir-europe.org", visa.getSub());
        Assert.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
        Assert.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
        Assert.assertEquals("affiliate@google.com", visa.getValue());
        Assert.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
        Assert.assertNull(visa.getConditions());
        Assert.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
    }

}
