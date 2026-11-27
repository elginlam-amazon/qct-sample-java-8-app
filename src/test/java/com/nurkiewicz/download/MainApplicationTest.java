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
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class MainApplicationTest {

	@LocalServerPort
	private int port;

	@Test
	void healthEndpointReportsUpOverRealHttp() throws Exception {
		final HttpResponse<String> response = get("/actuator/health");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"status\":\"UP\"");
	}

	@Test
	void servesFileContentOverRealHttp() throws Exception {
		final HttpResponse<String> response = get("/download/" + FileExamples.TXT_FILE_UUID + "/file.txt");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).isEqualTo("foobar");
		assertThat(response.headers().firstValue("ETag"))
				.hasValue(FileExamples.TXT_FILE.getEtag());
	}

	@Test
	void returnsNotFoundForUnknownUuidOverRealHttp() throws Exception {
		assertThat(get("/download/" + FileExamples.NOT_FOUND_UUID + "/file.txt").statusCode())
				.isEqualTo(404);
	}

	private HttpResponse<String> get(String path) throws Exception {
		try (HttpClient client = HttpClient.newHttpClient()) {
			return client.send(
					HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build(),
					HttpResponse.BodyHandlers.ofString());
		}
	}
}
