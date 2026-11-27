package org.springframework.web.filter;

import com.nurkiewicz.download.FilePointer;
import com.nurkiewicz.download.FileSystemPointer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Sha512ShallowEtagHeaderFilterTest {

	private static final byte[] CONTENT = "foobar".getBytes(StandardCharsets.UTF_8);

	// independently computed: printf 'foobar' | sha512sum
	private static final String SHA512_OF_CONTENT =
			"0a50261ebd1a390fed2bf326f2673c145582a6342d523204973d0219337f8161" +
			"6a8069b012587cf5635f6925f1b56c360230c19b273500ee013e030601bf2425";

	private final Sha512ShallowEtagHeaderFilter filter = new Sha512ShallowEtagHeaderFilter();

	@Test
	void generatesQuotedSha512StrongEtag() throws Exception {
		assertThat(filter.generateETagHeaderValue(new ByteArrayInputStream(CONTENT), false))
				.isEqualTo("\"" + SHA512_OF_CONTENT + "\"");
	}

	@Test
	void marksWeakEtagWithWeakPrefix() throws Exception {
		assertThat(filter.generateETagHeaderValue(new ByteArrayInputStream(CONTENT), true))
				.isEqualTo("W/\"" + SHA512_OF_CONTENT + "\"");
	}

	@Test
	void agreesWithTheEtagFileSystemPointerServes() throws Exception {
		// the controller's 304 handling only works if both sides emit the identical ETag form
		final FilePointer pointer = new FileSystemPointer(
				new File(getClass().getResource("/download.txt").toURI()));

		assertThat(filter.generateETagHeaderValue(new ByteArrayInputStream(CONTENT), false))
				.isEqualTo(pointer.getEtag());
	}
}
