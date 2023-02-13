package no.uio.ifi.clearinghouse;

import lombok.SneakyThrows;
import no.uio.ifi.clearinghouse.model.ByValue;
import no.uio.ifi.clearinghouse.model.Visa;
import no.uio.ifi.clearinghouse.model.VisaType;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

@RunWith(JUnit4.class)
public class ClearinghouseTests {

    private MockWebServer mockWebServer;
    private String passport;

    @SneakyThrows
    @Before
    public void init() {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        passport = Files.readString(Path.of("src/test/resources/passport.json"));
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

//    @SneakyThrows
//    @Test
//    public void getVisasTest() {
//        String accessToken = Files.readString(Path.of("src/test/resources/access-token.jwt"));
//        Collection<Visa> visas = Clearinghouse.INSTANCE.getVisas(accessToken, "https://login.elixir-czech.org/oidc/.well-known/openid-configuration");
//        Assert.assertEquals(1, visas.size());
//        Visa visa = visas.iterator().next();
//        Assert.assertEquals("test@elixir-europe.org", visa.getSub());
//        Assert.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
//        Assert.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
//        Assert.assertEquals("affiliate@google.com", visa.getValue());
//        Assert.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
//        Assert.assertNull(visa.getConditions());
//        Assert.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
//
//    }

//    @SneakyThrows
//    @Test
//    public void getVisasWithPEMPublicKeyTestTest() {
//        String accessToken = Files.readString(Path.of("src/test/resources/access-token.jwt"));
//        String publicKey = Files.readString(Path.of("src/test/resources/public.pem"));
//        Collection<Visa> visas = Clearinghouse.INSTANCE.getVisasWithPEMPublicKey(accessToken, publicKey);
//        Assert.assertEquals(1, visas.size());
//        Visa visa = visas.iterator().next();
//        Assert.assertEquals("test@elixir-europe.org", visa.getSub());
//        Assert.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
//        Assert.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
//        Assert.assertEquals("affiliate@google.com", visa.getValue());
//        Assert.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
//        Assert.assertNull(visa.getConditions());
//        Assert.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
//    }

//    @SneakyThrows
//    @Test
//    public void getVisaTest() {
//        String visaToken = Files.readString(Path.of("src/test/resources/visa.jwt"));
//        Optional<Visa> optionalVisa = Clearinghouse.INSTANCE.getVisa(visaToken);
//        Assert.assertTrue(optionalVisa.isPresent());
//        Visa visa = optionalVisa.get();
//        Assert.assertEquals("test@elixir-europe.org", visa.getSub());
//        Assert.assertEquals(VisaType.AffiliationAndRole.name(), visa.getType());
//        Assert.assertEquals(Long.valueOf(1583757401), visa.getAsserted());
//        Assert.assertEquals("affiliate@google.com", visa.getValue());
//        Assert.assertEquals("https://login.elixir-czech.org/google-idp/", visa.getSource());
//        Assert.assertNull(visa.getConditions());
//        Assert.assertEquals(ByValue.SYSTEM.name().toLowerCase(), visa.getBy());
//    }

    @SneakyThrows
    @Test
    public void getVisaWithPEMPublicKeyTest() {
        String visaToken = Files.readString(Path.of("src/test/resources/visa.jwt"));
        String publicKey = Files.readString(Path.of("src/test/resources/public.pem"));
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

//    @SneakyThrows
//    @Test
//    public void getVisaTokensTest() {
//        String accessToken = Files.readString(Path.of("src/test/resources/access-token.jwt"));
//        Collection<String> visaTokens = Clearinghouse.INSTANCE.getVisaTokens(accessToken, "https://login.elixir-czech.org/oidc/.well-known/openid-configuration");
//        Assert.assertEquals(1, visaTokens.size());
//        String visaToken = Files.readString(Path.of("src/test/resources/visa.jwt"));
//        Assert.assertEquals(visaToken, visaTokens.iterator().next() + "\n");
//    }

//    @SneakyThrows
//    @Test
//    public void getVisaTokensWithPEMPublicKeyTest() {
//        String accessToken = Files.readString(Path.of("src/test/resources/access-token.jwt"));
//        String publicKey = Files.readString(Path.of("src/test/resources/public.pem"));
//        Collection<String> visaTokens = Clearinghouse.INSTANCE.getVisaTokensWithPEMPublicKey(accessToken, publicKey);
//        Assert.assertEquals(1, visaTokens.size());
//        String visaToken = Files.readString(Path.of("src/test/resources/visa.jwt"));
//        Assert.assertEquals(visaToken, visaTokens.iterator().next() + "\n");
//    }

    @SneakyThrows
    @Test
    public void getVisaTokensFromOpaqueTokenTest() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(passport));
        HttpUrl url = mockWebServer.url("/test/getVisaTokensFromOpaqueTokenTest");

        String accessToken = Files.readString(Path.of("src/test/resources/access-token.jwt"));
        Collection<String> visaTokens = Clearinghouse.INSTANCE.getVisaTokensFromOpaqueToken(accessToken, url.toString());
        Assert.assertEquals(1, visaTokens.size());
        String visaToken = Files.readString(Path.of("src/test/resources/visa.jwt"));
        Assert.assertEquals(visaToken, visaTokens.iterator().next() + "\n");
    }

}
