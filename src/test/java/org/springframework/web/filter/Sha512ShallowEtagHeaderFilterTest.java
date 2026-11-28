package org.springframework.web.filter;

import com.google.common.hash.Hashing;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the SHA-512 ETag format across the Spring 7 change of
 * {@code generateETagHeaderValue} from {@code byte[]} to {@code (InputStream, boolean)}.
 */
class Sha512ShallowEtagHeaderFilterTest {

	private static final String CONTENT = "foobar";

	private final Sha512ShallowEtagHeaderFilter filter = new Sha512ShallowEtagHeaderFilter();

	@Test
	void shouldGenerateQuotedSha512AsStrongEtag() throws Exception {
		final String etag = filter.generateETagHeaderValue(stream(), false);

		assertThat(etag).isEqualTo("\"" + sha512OfContent() + "\"");
	}

	@Test
	void shouldPrefixWeakEtagWithW() throws Exception {
		final String etag = filter.generateETagHeaderValue(stream(), true);

		assertThat(etag).isEqualTo("W/\"" + sha512OfContent() + "\"");
	}

	@Test
	void shouldConsumeWholeStreamRegardlessOfLength() throws Exception {
		final byte[] large = new byte[8192];
		final String etag = filter.generateETagHeaderValue(new ByteArrayInputStream(large), false);

		assertThat(etag).isEqualTo("\"" + Hashing.sha512().hashBytes(large) + "\"");
	}

	private static String sha512OfContent() {
		return Hashing.sha512().hashString(CONTENT, StandardCharsets.UTF_8).toString();
	}

	private static InputStream stream() {
		return new ByteArrayInputStream(CONTENT.getBytes(StandardCharsets.UTF_8));
	}
}
