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
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Singleton class to to be used for getting visa JWT tokens provided access JWT token
 * and for converting visa JWT tokens to <code>Visa</code> POJOs.
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
    private static final String USERINFO = "userinfo";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    /**
     * Validates access JWT token and returns a list of Visas obtained from "/userinfo" endpoint.
     * Access token is validated based on JWKs URL of the OpenID configuration.
     * Visa tokens are validated based on JKUs.
     *
     * @param accessToken            Access JWT token.
     * @param openIDConfigurationURL ".well-known/openid-configuration" full URL.
     * @return List of GA4GH Visas.
     */
    public Collection<Visa> getVisas(String accessToken, String openIDConfigurationURL) {
        return getVisaTokens(accessToken, openIDConfigurationURL)
                .stream()
                .map(this::getVisa)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Validates access JWT token and returns a list of Visas obtained from "/userinfo" endpoint.
     * Access token is validated based on PEM RSA public key provided.
     * Visa tokens are validated based on JKUs.
     *
     * @param accessToken  Access JWT token.
     * @param pemPublicKey PEM RSA public key.
     * @return List of GA4GH Visas.
     */
    public Collection<Visa> getVisasWithPEMPublicKey(String accessToken, String pemPublicKey) {
        return getVisaTokensWithPEMPublicKey(accessToken, pemPublicKey)
                .stream()
                .map(this::getVisa)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Validates access JWT token and returns a list of Visas obtained from "/userinfo" endpoint.
     * Access token is validated based on RSA public key provided.
     * Visa tokens are validated based on JKUs.
     *
     * @param accessToken Access JWT token.
     * @param publicKey   RSA public key.
     * @return List of GA4GH Visas.
     */
    public Collection<Visa> getVisasWithPublicKey(String accessToken, RSAPublicKey publicKey) {
        return getVisaTokensWithPublicKey(accessToken, publicKey)
                .stream()
                .map(this::getVisa)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Validates visa JWT token and converts it to <code>Visa</code> POJO.
     * Token is validated based on JKU.
     *
     * @param visaToken Visa JWT token.
     * @return Optional <code>Visa</code> POJO: present if token validated successfully.
     */
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

    /**
     * Validates visa JWT token and converts it to <code>Visa</code> POJO.
     * Token is validated based on PEM RSA public key provided.
     *
     * @param visaToken    Visa JWT token.
     * @param pemPublicKey PEM RSA public key.
     * @return Optional <code>Visa</code> POJO: present if token validated successfully.
     */
    public Optional<Visa> getVisaWithPEMPublicKey(String visaToken, String pemPublicKey) {
        try {
            return getVisaWithPublicKey(visaToken, readPEMKey(pemPublicKey));
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Validates visa JWT token and converts it to <code>Visa</code> POJO.
     * Token is validated based on RSA public key provided.
     *
     * @param visaToken Visa JWT token.
     * @param publicKey RSA public key.
     * @return Optional <code>Visa</code> POJO: present if token validated successfully.
     */
    public Optional<Visa> getVisaWithPublicKey(String visaToken, RSAPublicKey publicKey) {
        var verifier = JWT.require(Algorithm.RSA256(publicKey, null)).build();
        try {
            return Optional.ofNullable(verifier.verify(visaToken).getClaim(GA_4_GH_VISA_V_1).as(Visa.class));
        } catch (JWTVerificationException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Validates access JWT token and returns a list of visa JWT tokens from "/userinfo" endpoint.
     * Access token is validated based on JWKs URL of the OpenID configuration.
     *
     * @param accessToken            Access JWT token.
     * @param openIDConfigurationURL ".well-known/openid-configuration" full URL.
     * @return List of visa JWT tokens.
     */
    public Collection<String> getVisaTokens(String accessToken, String openIDConfigurationURL) {
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

    /**
     * Validates access JWT token and returns a list of visa JWT tokens from "/userinfo" endpoint.
     * Access token is validated based on PEM RSA public key provided.
     *
     * @param accessToken  Access JWT token.
     * @param pemPublicKey PEM RSA public key.
     * @return List of visa JWT tokens.
     */
    public Collection<String> getVisaTokensWithPEMPublicKey(String accessToken, String pemPublicKey) {
        try {
            return getVisaTokensWithPublicKey(accessToken, readPEMKey(pemPublicKey));
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Validates access JWT token and returns a list of visa JWT tokens from "/userinfo" endpoint.
     * Access token is validated based on RSA public key provided.
     *
     * @param accessToken Access JWT token.
     * @param publicKey   RSA public key.
     * @return List of visa JWT tokens.
     */
    @SuppressWarnings("unchecked")
    public Collection<String> getVisaTokensWithPublicKey(String accessToken, RSAPublicKey publicKey) {
        var verifier = JWT.require(Algorithm.RSA256(publicKey, null)).build();
        var issuer = verifier.verify(accessToken).getIssuer();
        var userInfoEndpoint = issuer + USERINFO;
        var userInfo = Unirest.get(userInfoEndpoint).header(AUTHORIZATION, BEARER + accessToken).asJson();
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
