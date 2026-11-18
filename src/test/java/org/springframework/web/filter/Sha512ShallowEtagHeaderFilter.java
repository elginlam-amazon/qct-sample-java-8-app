package org.springframework.web.filter;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.io.IOException;
import java.io.InputStream;

/**
 * Custom ShallowEtagHeaderFilter that uses SHA-512 instead of the default MD5.
 * Updated for Spring 5.3: generateETagHeaderValue signature changed from byte[]
 * to InputStream + isWeak flag.
 */
public class Sha512ShallowEtagHeaderFilter extends ShallowEtagHeaderFilter {

    @Override
    protected String generateETagHeaderValue(InputStream inputStream, boolean isWeak) throws IOException {
        final byte[] bytes = inputStream.readAllBytes();
        final HashCode hash = Hashing.sha512().hashBytes(bytes);
        return "\"" + hash + "\"";
    }
}
