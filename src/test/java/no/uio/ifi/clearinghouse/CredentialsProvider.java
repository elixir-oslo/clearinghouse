package no.uio.ifi.clearinghouse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import no.uio.ifi.clearinghouse.model.Visa;

import java.security.*;
import java.util.Base64;
import java.util.Date;

public class CredentialsProvider {
    private final PrivateKey privateKey;
    private final String publicKeyString;
    private final String accessToken;
    private final String visaToken;

    public CredentialsProvider(String url) throws NoSuchAlgorithmException {
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        this.publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        this.accessToken = createAccessToken(url);
        this.visaToken = createVisaToken(url);
    }

    private String createAccessToken (String url) {
        return Jwts.builder()
                .setHeaderParam("kid", "rsa1")
                .setHeaderParam("alg", "RS256")
                .setSubject("test@elixir-europe.org")
                .setAudience("e84ce6d6-a136-4654-8128-14f034ea24f7")
                .claim("azp", "e84ce6d6-a136-4654-8128-14f034ea24f7")
                .claim("scope", "ga4gh_passport_v1 openid")
                .setIssuer(url)
                .setExpiration(new Date(32503680000000L))
                .setIssuedAt(new Date())
                .setId("03f5ca99-8df5-4d64-9dcb-7bf7701fe257")
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private String createVisaToken(String url) {
        Visa visa = new Visa();
        visa.setSub("test@elixir-europe.org");
        visa.setBy("system");
        visa.setType("AffiliationAndRole");
        visa.setAsserted(1583757401L);
        visa.setSource("https://login.elixir-czech.org/google-idp/");
        visa.setValue("affiliate@google.com");
        return Jwts.builder()
                .setHeaderParam("jku", "https://login.elixir-czech.org/oidc/jwk")
                .setHeaderParam("kid", "rsa1")
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "RS256")
                .setSubject("test@elixir-europe.org")
                .claim("ga4gh_visa_v1", visa)
                .setIssuer(url)
                .setExpiration(new Date(22001231))
                .setIssuedAt(new Date())
                .setId("f520d56f-e51a-431c-94e1-2a3f9da8b0c9")
                .compact();
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getPublicKeyString() {
        return this.publicKeyString;
    }

    public String getVisaToken() {
        return this.visaToken;
    }


}
