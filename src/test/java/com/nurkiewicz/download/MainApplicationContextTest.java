package com.nurkiewicz.download;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the full Spring Boot application context (mock servlet environment, no
 * server socket) so that auto-configuration is proven to start on this Spring
 * Boot line, not merely to compile.
 */
@SpringBootTest
@ActiveProfiles("test")
class MainApplicationContextTest {

	@Autowired
	private WebApplicationContext context;

	@Test
	void shouldStartApplicationContextWithDownloadEndpointWired() {
		assertThat(context.getBean(DownloadController.class)).isNotNull();
		assertThat(context.getBean(FileStorage.class)).isInstanceOf(FileStorageStub.class);
	}
}
