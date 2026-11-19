package org.springframework.web.filter;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.io.IOException;
import java.io.InputStream;

/**
 * Custom ShallowEtagHeaderFilter that generates ETags using SHA-512 instead of MD5/SHA-1.
 * Overrides the Spring 5.x API: generateETagHeaderValue(InputStream, boolean).
 */
public class Sha512ShallowEtagHeaderFilter extends ShallowEtagHeaderFilter {

    @Override
    protected String generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        final HashCode hash = Hashing.sha512().hashBytes(bytes);
        String prefix = isWeak ? "W/\"" : "\"";
        return prefix + hash + "\"";
    }
}
