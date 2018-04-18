package com.bca.boot.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 *
 * @author opaw
 */
public class TransactionInterceptor implements ClientHttpRequestInterceptor{
    private String accessToken;
    private String apiKey;
    private String apiSecret;

    public TransactionInterceptor(String accessToken, String apiKey, String apiSecret) {
        this.accessToken = accessToken;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }
    
    @Override
    public ClientHttpResponse intercept(final HttpRequest httpRequest, byte[] body, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {

        String timestamp = getTimestamp();

        HttpHeaders httpHeaders = httpRequest.getHeaders();
        httpHeaders.set("Authorization", "Bearer " + accessToken);
        httpHeaders.set("X-BCA-Key", apiKey);
        httpHeaders.set("X-BCA-Signature", sign(httpRequest, timestamp, new String(body)));
        httpHeaders.set("X-BCA-Timestamp", timestamp);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return clientHttpRequestExecution.execute(httpRequest, body);
    }

    private String sign(HttpRequest httpRequest, String timestamp, String body) {

        String text = httpRequest.getMethod().name() + ":" +
                getRequestPath(httpRequest) + ":" +
                accessToken + ":" +
                Hex.encodeHexString(sha256(body.replaceAll("\\s", ""))).toLowerCase() + ":" +
                timestamp;

        return hmacSha256(apiSecret, text);
    }

    private String getRequestPath(HttpRequest httpRequest) {
        StringBuilder builder = new StringBuilder();
        builder.append(httpRequest.getURI().getPath());
        if (httpRequest.getURI().getQuery() != null) {
            builder.append("?");
            builder.append(httpRequest.getURI().getQuery());
        }
        return builder.toString();
    }

    private byte[] sha256(String value) { return DigestUtils.sha256(value.getBytes()); }

    private String hmacSha256(String apiSecret, String value) {
        byte[] result = HmacUtils.hmacSha256(apiSecret, value);
        return new String(Hex.encodeHex(result));
    }

    private String getTimestamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return dateFormat.format(new Date());
    }
    
}
