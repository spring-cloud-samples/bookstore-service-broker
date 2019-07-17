/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.sample.bookstore.servicebroker.credhub;

import org.springframework.credhub.support.ServiceInstanceCredentialName;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

class CredHubPersistingWorkflow {

	private static final Logger LOG = Loggers.getLogger(CredHubPersistingWorkflow.class);

	private static final String CREDENTIALS_NAME = "credentials-json";
	private final String appName;

	CredHubPersistingWorkflow(String appName) {
		this.appName = appName;
	}

	Mono<ServiceInstanceCredentialName> buildCredentialName(String serviceDefinitionId, String bindingId) {
		LOG.debug("Building credentials name for service_id '{}' and binding_id '{}'", serviceDefinitionId, bindingId);
		return Mono.just(ServiceInstanceCredentialName.builder()
			.serviceBrokerName(this.appName)
			.serviceOfferingName(serviceDefinitionId)
			.serviceBindingId(bindingId)
			.credentialName(CREDENTIALS_NAME)
			.build());
	}
}
