package org.springframework.web.filter;

import com.nurkiewicz.download.FileExamples;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class Sha512ShallowEtagHeaderFilterTest {

	/**
	 * SHA-512 of the 6 bytes "foobar", i.e. the content of {@code src/test/resources/download.txt}.
	 */
	private static final String FOOBAR_SHA_512 =
			"0a50261ebd1a390fed2bf326f2673c145582a6342d523204973d0219337f8161" +
			"6a8069b012587cf5635f6925f1b56c360230c19b273500ee013e030601bf2425";

	private final Sha512ShallowEtagHeaderFilter filter = new Sha512ShallowEtagHeaderFilter();

	@Test
	void shouldGenerateQuotedSha512OfResponseBody() throws IOException {
		final String etag = generateEtag(false);

		assertThat(etag).isEqualTo("\"" + FOOBAR_SHA_512 + "\"");
	}

	@Test
	void shouldMarkWeakEtagWithWeakPrefix() throws IOException {
		final String etag = generateEtag(true);

		assertThat(etag).isEqualTo("W/\"" + FOOBAR_SHA_512 + "\"");
	}

	@Test
	void shouldGenerateSameEtagAsFilePointerForSameContent() throws IOException {
		assertThat(generateEtag(false)).isEqualTo(FileExamples.TXT_FILE.getEtag());
	}

	private String generateEtag(boolean weak) throws IOException {
		return filter.generateETagHeaderValue(new ByteArrayInputStream("foobar".getBytes(UTF_8)), weak);
	}
}
