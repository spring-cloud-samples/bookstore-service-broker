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

package org.springframework.cloud.broker.bookstore.webmvc.service;

import org.springframework.cloud.broker.bookstore.webmvc.model.ApplicationInformation;
import org.springframework.cloud.broker.bookstore.webmvc.model.ServiceBinding;
import org.springframework.cloud.broker.bookstore.webmvc.repository.ServiceBindingRepository;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.model.bindings.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.bindings.CreateServiceInstanceAppBindingResponse.CreateServiceInstanceAppBindingResponseBuilder;
import org.springframework.cloud.servicebroker.model.bindings.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.bindings.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.bindings.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookStoreServiceInstanceBindingService implements ServiceInstanceBindingService {
	private final ServiceBindingRepository repository;
	private final ApplicationInformation applicationInformation;
	private final InMemoryUserDetailsManager userService;

	public BookStoreServiceInstanceBindingService(ServiceBindingRepository repository,
												  ApplicationInformation applicationInformation,
												  InMemoryUserDetailsManager userService) {
		this.repository = repository;
		this.applicationInformation = applicationInformation;
		this.userService = userService;
	}

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
		CreateServiceInstanceAppBindingResponseBuilder responseBuilder =
				CreateServiceInstanceAppBindingResponse.builder();

		Optional<ServiceBinding> binding = repository.findById(request.getBindingId());

		if (binding.isPresent()) {
			responseBuilder
					.bindingExisted(true)
					.credentials(binding.get().getCredentials());
		} else {
			Map<String, Object> credentials = buildCredentials(request.getServiceInstanceId(), request.getBindingId());
			
			responseBuilder
					.bindingExisted(false)
					.credentials(credentials);

			persistBinding(request, credentials);
		}

		return responseBuilder.build();
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
		String bindingId = request.getBindingId();

		if (repository.existsById(bindingId)) {
			userService.deleteUser(bindingId);
			repository.deleteById(bindingId);
		} else {
			throw new ServiceInstanceBindingDoesNotExistException(bindingId);
		}
	}

	private Map<String, Object> buildCredentials(String instanceId, String bindingId) {
		String uri = buildUri(instanceId);

		String password = createUser(bindingId);

		Map<String, Object> credentials = new HashMap<>();
		credentials.put("uri", uri);
		credentials.put("username", bindingId);
		credentials.put("password", password);

		return credentials;
	}

	private String buildUri(String instanceId) {
		return UriComponentsBuilder
					.fromUriString(applicationInformation.getBaseUrl())
					.pathSegment("bookstores", instanceId)
					.pathSegment("books")
					.build()
					.toUriString();
	}

	private String createUser(String username) {
		String password = UUID.randomUUID().toString();

		userService.createUser(
				User.withDefaultPasswordEncoder()
						.username(username)
						.password(password)
						.roles("USER")
						.build());

		return password;
	}

	private void persistBinding(CreateServiceInstanceBindingRequest request, Map<String, Object> credentials) {
		ServiceBinding serviceBinding =
				new ServiceBinding(request.getBindingId(), request.getContext(), credentials);
		repository.save(serviceBinding);
	}
}