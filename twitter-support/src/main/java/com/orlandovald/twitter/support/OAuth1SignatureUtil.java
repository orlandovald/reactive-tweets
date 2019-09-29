package com.orlandovald.twitter.support;

import com.twitter.joauth.Normalizer;
import com.twitter.joauth.OAuthParams;
import com.twitter.joauth.Request;
import com.twitter.joauth.Signer;
import io.reactivex.Single;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility methods to generate Twitters' OAuth header
 *
 * Provides methods to return the OAuth header as a String, RxJava's Single and Reactor's Mono
 */
public class OAuth1SignatureUtil {

    private static final String OAUTH1_HEADER_AUTHTYPE = "OAuth ";
    private static final String OAUTH_TOKEN = "oauth_token";
    private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String OAUTH_SIGNATURE = "oauth_signature";
    private static final String OAUTH_NONCE = "oauth_nonce";
    private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    private static final String OAUTH_VERSION = "oauth_version";
    private static final String HMAC_SHA1 = "HMAC-SHA1";
    private static final String ONE_DOT_OH = "1.0";
    private static final String UTF8_CHARSET_NAME = "UTF-8";

    private final Normalizer normalizer;
    private final Signer signer;
    private final SecureRandom secureRandom;
    private final String accessToken;
    private final String accessTokenSecret;
    private final String consumerKey;
    private final String consumerSecret;


    public OAuth1SignatureUtil(String accessToken, String accessTokenSecret, String consumerKey, String consumerSecret) {
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.normalizer = Normalizer.getStandardNormalizer();
        this.signer = Signer.getStandardSigner();
        this.secureRandom = new SecureRandom();
    }

    /**
     * Returns the OAuth header as a RxJava Single&lt;String&gt;
     */
    public Single<String> oAuth1HeaderAsSingle(URI uri, String method) {
        return Single.just(oAuth1Header(uri, method));
    }

    /**
     * Returns the OAuth header as a Reactor Mono&lt;String&gt;
     */
    public Mono<String> oAuth1HeaderAsMongo(URI uri, String method) {
        return Mono.just(oAuth1Header(uri, method));
    }

    /**
     * Returns the OAuth header as a String
     */
    public String oAuth1Header(URI uri, String method) {

        List<Request.Pair> requestParams = new ArrayList<>();
        for (Map.Entry<String, String> entry : parseQueryString(uri.getRawQuery()).entrySet()) {
            requestParams.add(new Request.Pair(urlEncode(entry.getKey()), urlEncode(entry.getValue())));
        }

        long timestampSecs = this.generateTimestamp();
        String nonce = this.generateNonce();
        OAuthParams.OAuth1Params oAuth1Params = new OAuthParams.OAuth1Params(
                this.accessToken, this.consumerKey, nonce, Long.valueOf(timestampSecs),
                Long.toString(timestampSecs), "", HMAC_SHA1, ONE_DOT_OH);


        int port = getPort(uri);

        String normalized = this.normalizer.normalize(uri.getScheme(), uri.getHost(), port, method.toUpperCase(),
                uri.getPath(), requestParams, oAuth1Params);

        String signature;
        try {
            signature = this.signer.getString(normalized, this.accessTokenSecret, this.consumerSecret);
        } catch (InvalidKeyException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }

        Map<String, String> oauthHeaders = new HashMap<>();
        oauthHeaders.put(OAUTH_CONSUMER_KEY, this.quoted(this.consumerKey));
        oauthHeaders.put(OAUTH_TOKEN, this.quoted(this.accessToken));
        oauthHeaders.put(OAUTH_SIGNATURE, this.quoted(signature));
        oauthHeaders.put(OAUTH_SIGNATURE_METHOD, this.quoted(HMAC_SHA1));
        oauthHeaders.put(OAUTH_TIMESTAMP, this.quoted(Long.toString(timestampSecs)));
        oauthHeaders.put(OAUTH_NONCE, this.quoted(nonce));
        oauthHeaders.put(OAUTH_VERSION, this.quoted(ONE_DOT_OH));

        return OAUTH1_HEADER_AUTHTYPE
                + oauthHeaders.entrySet().stream().map(Map.Entry::toString).collect(Collectors.joining(", "));

    }

    private int getPort(URI uri) {
        int port = uri.getPort();

        if (port <= 0) {
            if (uri.getScheme().equalsIgnoreCase("http")) {
                port = 80;
            } else {
                if (!uri.getScheme().equalsIgnoreCase("https")) {
                    throw new IllegalStateException("Bad URI scheme: " + uri.getScheme());
                }
                port = 443;
            }
        }
        return port;
    }

    private static String formDecode(String encoded) {
        try {
            return URLDecoder.decode(encoded, UTF8_CHARSET_NAME);
        } catch (UnsupportedEncodingException shouldntHappen) {
            throw new IllegalStateException(shouldntHappen);
        }
    }

    private Map<String, String> parseQueryString(String parameterString) {
        if (parameterString == null || parameterString.length() == 0) {
            return new HashMap<>();
        }
        String[] pairs = parameterString.split("&");
        LinkedHashMap<String, String> result = new LinkedHashMap<>(pairs.length);
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) {
                result.put(formDecode(pair), "");
            } else {
                String name = formDecode(pair.substring(0, idx));
                String value = formDecode(pair.substring(idx + 1));
                result.put(name, value);
            }
        }
        return result;
    }

    private String quoted(String str) {
        return "\"" + str + "\"";
    }

    private long generateTimestamp() {
        long timestamp = System.currentTimeMillis();
        return timestamp / 1000L;
    }

    private String generateNonce() {
        return Long.toString(Math.abs(this.secureRandom.nextLong())) + System.currentTimeMillis();
    }

    private String urlEncode(String source) {
        if (source == null) {
            return "";
        }
        try {
            return URLEncoder.encode(source, UTF8_CHARSET_NAME)
                    // OAuth specific encoding
                    .replace("+", "%20").replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException wow) {
            throw new RuntimeException(wow.getMessage(), wow);
        }
    }

}
