package com.mentalhealth.mentalhealth_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // uses src/test/resources/application-test.properties (H2, no MySQL needed)
class MentalhealthBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
