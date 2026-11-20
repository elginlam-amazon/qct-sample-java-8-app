package org.springframework.web.filter;

import com.nurkiewicz.download.FileExamples;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class Sha512ShallowEtagHeaderFilterTest {

	/**
	 * SHA-512 of the six bytes 'foobar', the content of /download.txt.
	 */
	private static final String FOOBAR_SHA512 =
			"0a50261ebd1a390fed2bf326f2673c145582a6342d523204973d0219337f8161" +
			"6a8069b012587cf5635f6925f1b56c360230c19b273500ee013e030601bf2425";

	private final Sha512ShallowEtagHeaderFilter filter = new Sha512ShallowEtagHeaderFilter();

	@Test
	void shouldGenerateStrongSha512Etag() throws IOException {
		final String etag = filter.generateETagHeaderValue(body(), false);

		assertThat(etag).isEqualTo("\"" + FOOBAR_SHA512 + "\"");
	}

	@Test
	void shouldMarkEtagAsWeakWhenRequested() throws IOException {
		final String etag = filter.generateETagHeaderValue(body(), true);

		assertThat(etag).isEqualTo("W/\"" + FOOBAR_SHA512 + "\"");
	}

	@Test
	void shouldAgreeWithEtagComputedFromTheFileItself() throws IOException {
		final String etag = filter.generateETagHeaderValue(body(), false);

		assertThat(etag).isEqualTo(FileExamples.TXT_FILE.getEtag());
	}

	private static ByteArrayInputStream body() {
		return new ByteArrayInputStream("foobar".getBytes(UTF_8));
	}
}
