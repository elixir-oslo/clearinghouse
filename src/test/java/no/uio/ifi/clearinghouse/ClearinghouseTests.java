package no.uio.ifi.clearinghouse;

import kong.unirest.Unirest;
import lombok.SneakyThrows;
import no.uio.ifi.clearinghouse.model.ByValue;
import no.uio.ifi.clearinghouse.model.Visa;
import no.uio.ifi.clearinghouse.model.VisaType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.net.ssl.*"})
@PrepareForTest({Unirest.class})
public class ClearinghouseTests {

    @SneakyThrows
    @Before
    public void init() {
        PowerMockito.spy(Unirest.class);
        String jwk = Files.readString(Path.of("src/test/resources/jwk.json"));
        BDDMockito.given(Unirest.get("https://login.elixir-czech.org/oidc/jwk")).willReturn(new GetRequestStub(jwk));
        String passport = Files.readString(Path.of("src/test/resources/passport.json"));
        BDDMockito.given(Unirest.get("https://login.elixir-czech.org/oidc/userinfo")).willReturn(new GetRequestStub(passport));
    }

    @Test
    public void getVisaTest() throws IOException {
        String visaToken = Files.readString(Path.of("src/test/resources/visa.jwt"));
        Optional<Visa> optionalVisa = Clearinghouse.INSTANCE.getVisa(visaToken);
        Assert.assertTrue(optionalVisa.isPresent());
        Visa visa = optionalVisa.get();
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
        String visaToken = Files.readString(Path.of("src/test/resources/visa.jwt"));
        String publicKey = Files.readString(Path.of("src/test/resources/public.pem"));
        Optional<Visa> optionalVisa = Clearinghouse.INSTANCE.getVisaWithPEMPublicKey(visaToken, publicKey);
        Assert.assertTrue(optionalVisa.isPresent());
        Visa visa = optionalVisa.get();
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
        String accessToken = Files.readString(Path.of("src/test/resources/visa.jwt"));
        List<String> visaTokens = Clearinghouse.INSTANCE.getVisaTokens(accessToken, "https://login.elixir-czech.org/oidc/.well-known/openid-configuration");
        Assert.assertEquals(1, visaTokens.size());
        String visaToken = Files.readString(Path.of("src/test/resources/visa.jwt"));
        Assert.assertEquals(visaToken, visaTokens.iterator().next() + "\n");
    }

    @SneakyThrows
    @Test
    public void getVisaTokensWithPEMPublicKeyTest() {
        String accessToken = Files.readString(Path.of("src/test/resources/visa.jwt"));
        String publicKey = Files.readString(Path.of("src/test/resources/public.pem"));
        List<String> visaTokens = Clearinghouse.INSTANCE.getVisaTokensWithPEMPublicKey(accessToken, publicKey);
        Assert.assertEquals(1, visaTokens.size());
        String visaToken = Files.readString(Path.of("src/test/resources/visa.jwt"));
        Assert.assertEquals(visaToken, visaTokens.iterator().next() + "\n");
    }

}
