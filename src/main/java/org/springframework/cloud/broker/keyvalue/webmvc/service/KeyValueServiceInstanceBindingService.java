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

package org.springframework.cloud.broker.keyvalue.webmvc.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class KeyValueServiceInstanceBindingService implements ServiceInstanceBindingService {
	private final ApplicationInformation applicationInformation;
	private UserDetails defaultUser;

	public KeyValueServiceInstanceBindingService(ApplicationInformation applicationInformation,
												 @Qualifier("defaultUser") UserDetails defaultUser) {
		this.applicationInformation = applicationInformation;
		this.defaultUser = defaultUser;
	}

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
		String uri = UriComponentsBuilder
				.fromPath(applicationInformation.getBaseUrl())
				.pathSegment("keyvalue", request.getServiceInstanceId())
				.build()
				.toUriString();

		return CreateServiceInstanceAppBindingResponse.builder()
				.credentials("uri", uri)
				.credentials("username", defaultUser.getUsername())
				.credentials("password", defaultUser.getPassword())
				.build();
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
	}
}