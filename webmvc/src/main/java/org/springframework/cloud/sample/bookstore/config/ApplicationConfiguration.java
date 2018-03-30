/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.cloud.sample.bookstore.web.model.ApplicationInformation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class ApplicationConfiguration {
	@Bean
	@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
	public ApplicationInformation cloudFoundryApplicationInformation(Environment environment) {
		String uri = environment.getProperty("vcap.application.uris[0]");

		String baseUrl = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(uri)
				.build()
				.toUriString();

		return new ApplicationInformation(baseUrl);
	}

	@Bean
	@ConditionalOnProperty("KUBERNETES_SERVICE_HOST")
	public ApplicationInformation kubernetesApplicationInformation(Environment environment) {
		String uri = environment.getProperty("KUBERNETES_SERVICE_HOST");
		String port = environment.getProperty("KUBERNETES_SERVICE_PORT");
		
		String baseUrl = UriComponentsBuilder.newInstance()
			.scheme("https")
			.host(uri)
			.port(port)
			.build()
			.toUriString();

		return new ApplicationInformation(baseUrl);
	}
	
	@Bean
	@ConditionalOnMissingBean(ApplicationInformation.class)
	public ApplicationInformation defaultApplicationInformation() {
		String baseUrl = UriComponentsBuilder.newInstance()
				.scheme("http")
				.host("localhost")
				.port(8080)
				.build()
				.toUriString();

		return new ApplicationInformation(baseUrl);
	}
}
