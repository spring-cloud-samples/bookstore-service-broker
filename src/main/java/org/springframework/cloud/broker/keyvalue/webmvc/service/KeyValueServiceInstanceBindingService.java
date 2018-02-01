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
import org.springframework.cloud.broker.keyvalue.webmvc.model.ApplicationInformation;
import org.springframework.cloud.broker.keyvalue.webmvc.model.ServiceBinding;
import org.springframework.cloud.broker.keyvalue.webmvc.repository.ServiceBindingRepository;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class KeyValueServiceInstanceBindingService implements ServiceInstanceBindingService {
	private final ServiceBindingRepository repository;
	private final ApplicationInformation applicationInformation;
	private final UserDetails defaultUser;

	public KeyValueServiceInstanceBindingService(ServiceBindingRepository repository,
												 ApplicationInformation applicationInformation,
												 @Qualifier("defaultUser") UserDetails defaultUser) {
		this.repository = repository;
		this.applicationInformation = applicationInformation;
		this.defaultUser = defaultUser;
	}

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
		Optional<ServiceBinding> binding = repository.findById(request.getBindingId());

		Map<String, Object> credentials;
		if (binding.isPresent()) {
			credentials = binding.get().getCredentials();
		} else {
			credentials = buildCredentials(request.getServiceInstanceId());
			persistBinding(request, credentials);
		}

		return CreateServiceInstanceAppBindingResponse.builder()
				.bindingExisted(binding.isPresent())
				.credentials(credentials)
				.build();
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
		String bindingId = request.getBindingId();

		if (repository.existsById(bindingId)) {
			repository.deleteById(bindingId);
		} else {
			throw new ServiceInstanceBindingDoesNotExistException(bindingId);
		}
	}

	private Map<String, Object> buildCredentials(String id) {
		String uri = UriComponentsBuilder
				.fromUriString(applicationInformation.getBaseUrl())
				.pathSegment("keyvalue", id)
				.build()
				.toUriString();

		Map<String, Object> credentials = new HashMap<>();
		credentials.put("uri", uri);
		credentials.put("username", defaultUser.getUsername());
		credentials.put("password", defaultUser.getPassword());

		return credentials;
	}

	private void persistBinding(CreateServiceInstanceBindingRequest request, Map<String, Object> credentials) {
		ServiceBinding serviceBinding = new ServiceBinding(request.getBindingId(), request.getContext(),
				credentials);
		repository.save(serviceBinding);
	}
}