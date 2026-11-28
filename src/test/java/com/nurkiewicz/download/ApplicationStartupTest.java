package com.nurkiewicz.download;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the application on a real embedded servlet container, which the MockMvc
 * specs never do. This is what proves the upgraded stack actually starts.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApplicationStartupTest {

	@LocalServerPort
	private int port;

	@Test
	void shouldReportHealthUp() throws Exception {
		final HttpResponse<String> response = get("/actuator/health");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"status\":\"UP\"");
	}

	@Test
	void shouldStreamFileThroughRealContainer() throws Exception {
		final HttpResponse<String> response = get("/download/" + FileExamples.TXT_FILE_UUID + "/file.txt");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).isEqualTo("foobar");
		assertThat(response.headers().firstValue("ETag")).hasValue(FileExamples.TXT_FILE.getEtag());
	}

	@Test
	void shouldReturn404ForUnknownFile() throws Exception {
		final HttpResponse<String> response = get("/download/" + FileExamples.NOT_FOUND_UUID + "/file.txt");

		assertThat(response.statusCode()).isEqualTo(404);
	}

	private HttpResponse<String> get(String path) throws Exception {
		final HttpRequest request = HttpRequest
				.newBuilder(URI.create("http://localhost:" + port + path))
				.GET()
				.build();
		try (HttpClient client = HttpClient.newHttpClient()) {
			return client.send(request, HttpResponse.BodyHandlers.ofString());
		}
	}
}
