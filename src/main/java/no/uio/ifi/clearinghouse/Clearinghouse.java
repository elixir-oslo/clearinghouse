package no.uio.ifi.clearinghouse;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import no.uio.ifi.clearinghouse.model.Visa;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Main class of the library, encapsulating TSD File API client methods.
 */
@Slf4j
public enum Clearinghouse {

    INSTANCE;

    private static final String KEY_WRAPPING = "-----(.*?)-----";
    private static final String JKU = "jku";
    private static final String RSA = "RSA";
    private static final String JWKS_URI = "jwks_uri";
    private static final String GA_4_GH_PASSPORT_V_1 = "ga4gh_passport_v1";
    private static final String GA_4_GH_VISA_V_1 = "ga4gh_visa_v1";

    public Optional<Visa> getVisa(String visaToken) {
        var decodedToken = JWT.decode(visaToken);
        var jku = decodedToken.getHeaderClaim(JKU).asString();
        var keyId = decodedToken.getKeyId();
        var jwk = JWKProvider.INSTANCE.get(jku, keyId);
        try {
            return getVisaWithPublicKey(visaToken, (RSAPublicKey) jwk.getPublicKey());
        } catch (InvalidPublicKeyException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<Visa> getVisaWithPEMPublicKey(String visaToken, String pemPublicKey) {
        try {
            return getVisaWithPublicKey(visaToken, readPEMKey(pemPublicKey));
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<Visa> getVisaWithPublicKey(String visaToken, RSAPublicKey publicKey) {
        var verifier = JWT.require(Algorithm.RSA256(publicKey, null)).build();
        try {
            return Optional.ofNullable(verifier.verify(visaToken).getClaim(GA_4_GH_VISA_V_1).as(Visa.class));
        } catch (JWTVerificationException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public List<String> getVisaTokens(String accessToken, String openIDConfigurationURL) {
        var openIDConfiguration = Unirest.get(openIDConfigurationURL).asJson();
        var jwksURL = openIDConfiguration.getBody().getObject().getString(JWKS_URI);
        var decodedToken = JWT.decode(accessToken);
        var keyId = decodedToken.getKeyId();
        var jwk = JWKProvider.INSTANCE.get(jwksURL, keyId);
        try {
            return getVisaTokensWithPublicKey(accessToken, (RSAPublicKey) jwk.getPublicKey());
        } catch (InvalidPublicKeyException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<String> getVisaTokensWithPEMPublicKey(String accessToken, String pemPublicKey) {
        try {
            return getVisaTokensWithPublicKey(accessToken, readPEMKey(pemPublicKey));
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getVisaTokensWithPublicKey(String accessToken, RSAPublicKey publicKey) {
        var verifier = JWT.require(Algorithm.RSA256(publicKey, null)).build();
        var issuer = verifier.verify(accessToken).getIssuer();
        var userInfoEndpoint = issuer + "userinfo";
        var userInfo = Unirest.get(userInfoEndpoint).header("Authorization", "Bearer " + accessToken).asJson();
        var passport = userInfo.getBody().getObject().getJSONArray(GA_4_GH_PASSPORT_V_1);
        return passport.toList();
    }

    private RSAPublicKey readPEMKey(String publicKey) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        publicKey = publicKey.replaceAll(KEY_WRAPPING, "").replace(System.lineSeparator(), "").replace(" ", "").trim();
        var decodedKey = Base64.getDecoder().decode(publicKey);
        return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
    }

}
