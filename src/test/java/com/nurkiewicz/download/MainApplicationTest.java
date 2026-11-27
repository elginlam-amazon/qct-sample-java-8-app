package com.nurkiewicz.download;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Starts the application in an embedded Tomcat bound to a real socket, as opposed to the
 * mock servlet environment used by {@code DownloadControllerSpec}.
 */
@SpringBootTest(classes = MainApplication.class, webEnvironment = RANDOM_PORT)
class MainApplicationTest {

	@LocalServerPort
	private int port;

	@Test
	void shouldStartEmbeddedServletContainerAndServeActuatorHealth() {
		final ResponseEntity<String> response = RestClient
				.create()
				.get()
				.uri("http://localhost:" + port + "/actuator/health")
				.retrieve()
				.toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("\"status\":\"UP\"");
	}
}
