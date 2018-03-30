/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.sample.bookstore.config;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.sample.bookstore.web.model.ApplicationInformation;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationConfigurationTests {
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(ApplicationConfiguration.class));

	@Test
	public void cloudFoundryApplicationInfo() {
		this.contextRunner
				.withPropertyValues("VCAP_APPLICATION: {" +
						" \"application_id\": \"ede01bcb-bbc2-4bd4-ac7b-b56be151afc3\"," +
						" \"uris\": [" +
						"  \"route.apps.example.com\"," +
						"  \"other-route.apps.example.com\"" +
						" ]" +
						"}",
						"vcap.application.uris[0]=route.apps.example.com",
						"vcap.application.uris[1]=other-route.apps.example.com")
				.run((context) ->
					assertThat(context)
						.getBean(ApplicationInformation.class)
						.hasFieldOrPropertyWithValue("baseUrl", "https://route.apps.example.com")
				);
	}

	@Test
	public void kubernetesApplicationInfo() {
		this.contextRunner
				.withPropertyValues("KUBERNETES_SERVICE_HOST=10.10.10.10",
						"KUBERNETES_SERVICE_PORT=443")
				.run((context) ->
						assertThat(context)
								.getBean(ApplicationInformation.class)
								.hasFieldOrPropertyWithValue("baseUrl", "https://10.10.10.10:443")
				);
	}
	
	@Test
	public void localApplicationInfo() {
		this.contextRunner
				.run((context) ->
					assertThat(context)
							.getBean(ApplicationInformation.class)
							.hasFieldOrPropertyWithValue("baseUrl", "http://localhost:8080")
				);
	}
}
