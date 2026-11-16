package org.springframework.web.filter;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class Sha512ShallowEtagHeaderFilter extends ShallowEtagHeaderFilter {

	@Override
	protected String generateETagHeaderValue(InputStream inputStream, boolean isWeak) {
		final byte[] bytes;
		try {
			bytes = ByteStreams.toByteArray(inputStream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		final HashCode hash = Hashing.sha512().hashBytes(bytes);
		return (isWeak ? "W/\"" : "\"") + hash + "\"";
	}
}
