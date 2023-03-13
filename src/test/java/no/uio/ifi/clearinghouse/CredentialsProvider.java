package no.uio.ifi.clearinghouse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import no.uio.ifi.clearinghouse.model.Visa;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;

public class CredentialsProvider {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String accessToken;
    private final String visaToken;
    private final String passportJsonString;
    private final String jwkJsonString;

    public CredentialsProvider(String url) throws Exception {
        File privateKeyFile = new File("src/test/resources/private.pem");
        File publicKeyFile = new File("src/test/resources/public.pem");
        this.privateKey = readPrivateKey(privateKeyFile);
        this.publicKey = readPublicKey(publicKeyFile);

        this.accessToken = createAccessToken(url);
        this.visaToken = createVisaToken(url);

        this.passportJsonString = createPassportJsonString();
        this.jwkJsonString = createJwkJsonString();
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
                .signWith(this.privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private String createVisaToken(String url) {
        Visa visa = new Visa();
        visa.setBy("system");
        visa.setType("AffiliationAndRole");
        visa.setAsserted(1583757401L);
        visa.setSource("https://login.elixir-czech.org/google-idp/");
        visa.setValue("affiliate@google.com");
        return Jwts.builder()
                .setHeaderParam("jku", url + "jwk")
                .setHeaderParam("kid", "rsa1")
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "RS256")
                .setSubject("test@elixir-europe.org")
                .claim("ga4gh_visa_v1", visa)
                .setIssuer(url)
                .setExpiration(new Date(32503680000000L))
                .setIssuedAt(new Date())
                .setId("f520d56f-e51a-431c-94e1-2a3f9da8b0c9")
                .signWith(this.privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private RSAPrivateKey readPrivateKey(File file) throws IOException {
        Security.addProvider(new BouncyCastleProvider());
        PEMParser pemParser = new PEMParser(new FileReader(file));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        Object object = pemParser.readObject();
        KeyPair kp = converter.getKeyPair((PEMKeyPair) object);

        return (RSAPrivateKey) kp.getPrivate();
    }

    private RSAPublicKey readPublicKey(File file) throws IOException {
        try (FileReader keyReader = new FileReader(file)) {
            PEMParser pemParser = new PEMParser(keyReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pemParser.readObject());
            return (RSAPublicKey) converter.getPublicKey(publicKeyInfo);
        }
    }

    private String toPEM(PrivateKey privateKey) {
        String encoded = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PRIVATE KEY-----\n");
        int len = encoded.length();
        for (int i = 0; i < len; i += 64) {
            pem.append(encoded, i, Math.min(len, i + 64));
            pem.append("\n");
        }
        pem.append("-----END PRIVATE KEY-----\n");
        return pem.toString();
    }

    // create passport.json w/ the newly generated visaToken

    private String createPassportJsonString() {
        return "{\n" +
                "  \"sub\": \"test@elixir-europe.org\",\n" +
                "  \"ga4gh_passport_v1\": [\n" +
                "    \"" + this.visaToken + "\"" +
                "  ]\n" +
                "}";
    }

    private String createJwkJsonString() {
        RSAPublicKey publicKey = (RSAPublicKey) this.publicKey;

        return "{\n" +
                "  \"keys\": [\n" +
                "    {\n" +
                "      \"kty\": \"RSA\",\n" +
                "      \"e\": \"AQAB\",\n" +
                "      \"kid\": \"rsa1\",\n" +
                "      \"n\": \"" + publicKey.getModulus().toString(16) + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }


    public String getAccessToken() {
        return this.accessToken;
    }

    public String getVisaToken() {
        return this.visaToken + "\n";
    }

    public String getPassportJsonString() {
        return this.passportJsonString;
    }

}
