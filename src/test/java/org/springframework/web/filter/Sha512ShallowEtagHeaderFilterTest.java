package org.springframework.web.filter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

class Sha512ShallowEtagHeaderFilterTest {

	private static final byte[] CONTENT = "foobar".getBytes(StandardCharsets.UTF_8);

	private final Sha512ShallowEtagHeaderFilter filter = new Sha512ShallowEtagHeaderFilter();

	@Test
	void shouldGenerateStrongEtagFromSha512OfContent() throws Exception {
		final String etag = filter.generateETagHeaderValue(new ByteArrayInputStream(CONTENT), false);

		assertThat(etag).isEqualTo("\"" + sha512Hex(CONTENT) + "\"");
	}

	@Test
	void shouldMarkEtagAsWeakWhenBaseClassAsksForIt() throws Exception {
		final String etag = filter.generateETagHeaderValue(new ByteArrayInputStream(CONTENT), true);

		assertThat(etag).isEqualTo("W/\"" + sha512Hex(CONTENT) + "\"");
	}

	/**
	 * Independent oracle: the JDK's own SHA-512, so the expected value does not come
	 * from the same Guava {@code Hashing} code the filter uses.
	 */
	private static String sha512Hex(byte[] bytes) throws NoSuchAlgorithmException {
		final byte[] digest = MessageDigest.getInstance("SHA-512").digest(bytes);
		final StringBuilder hex = new StringBuilder(digest.length * 2);
		for (byte b : digest) {
			hex.append(String.format("%02x", b));
		}
		return hex.toString();
	}
}
