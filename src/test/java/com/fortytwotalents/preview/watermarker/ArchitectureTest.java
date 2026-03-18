package com.fortytwotalents.preview.watermarker;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

	private static final String BASE_PACKAGE = "com.fortytwotalents.preview.watermarker";

	@Test
	void shouldFollowNamingConventions() {
		Taikai.builder()
			.namespace(BASE_PACKAGE)
			.java(java -> java
				.noUsageOf(Thread.class)
				.imports(imports -> imports
					.shouldHaveNoCycles()
					.shouldNotImport("sun..")
					.shouldNotImport("com.sun..")))
			.spring(spring -> spring
				.noAutowiredFields()
				.configurations(configurations -> configurations
					.namesShouldEndWithConfiguration()
					.namesShouldMatch(".*Configuration"))
				.services(services -> services
					.namesShouldEndWithService()
					.namesShouldMatch(".*Service"))
				.repositories(repositories -> repositories
					.namesShouldEndWithRepository()
					.namesShouldMatch(".*Repository")))
			.build()
			.check();
	}

}
