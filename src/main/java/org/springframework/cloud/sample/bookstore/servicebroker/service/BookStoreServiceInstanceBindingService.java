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

package org.springframework.cloud.sample.bookstore.servicebroker.service;

import org.springframework.cloud.sample.bookstore.web.model.ApplicationInformation;
import org.springframework.cloud.sample.bookstore.servicebroker.model.ServiceBinding;
import org.springframework.cloud.sample.bookstore.servicebroker.repository.ServiceBindingRepository;
import org.springframework.cloud.sample.bookstore.web.model.User;
import org.springframework.cloud.sample.bookstore.web.service.UserService;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse.CreateServiceInstanceAppBindingResponseBuilder;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.GetServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.GetServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.GetServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.FULL_ACCESS;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.BOOK_STORE_ID_PREFIX;

@Service
public class BookStoreServiceInstanceBindingService implements ServiceInstanceBindingService {
	private static final String URI_KEY = "uri";
	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";

	private final ServiceBindingRepository bindingRepository;
	private final UserService userService;
	private final ApplicationInformation applicationInformation;

	public BookStoreServiceInstanceBindingService(ServiceBindingRepository bindingRepository,
												  UserService userService,
												  ApplicationInformation applicationInformation) {
		this.bindingRepository = bindingRepository;
		this.userService = userService;
		this.applicationInformation = applicationInformation;
	}

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
		CreateServiceInstanceAppBindingResponseBuilder responseBuilder =
				CreateServiceInstanceAppBindingResponse.builder();

		Optional<ServiceBinding> binding = bindingRepository.findById(request.getBindingId());

		if (binding.isPresent()) {
			responseBuilder
					.bindingExisted(true)
					.credentials(binding.get().getCredentials());
		} else {
			User user = createUser(request);

			Map<String, Object> credentials = buildCredentials(request.getServiceInstanceId(), user);
			saveBinding(request, credentials);

			responseBuilder
					.bindingExisted(false)
					.credentials(credentials);
		}

		return responseBuilder.build();
	}

	@Override
	public GetServiceInstanceBindingResponse getServiceInstanceBinding(GetServiceInstanceBindingRequest request) {
		String bindingId = request.getBindingId();

		Optional<ServiceBinding> serviceBinding = bindingRepository.findById(bindingId);

		if (serviceBinding.isPresent()) {
			return GetServiceInstanceAppBindingResponse.builder()
					.parameters(serviceBinding.get().getParameters())
					.credentials(serviceBinding.get().getCredentials())
					.build();
		} else {
			throw new ServiceInstanceBindingDoesNotExistException(bindingId);
		}
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
		String bindingId = request.getBindingId();

		if (bindingRepository.existsById(bindingId)) {
			bindingRepository.deleteById(bindingId);
			userService.deleteUser(bindingId);
		} else {
			throw new ServiceInstanceBindingDoesNotExistException(bindingId);
		}
	}

	private User createUser(CreateServiceInstanceBindingRequest request) {
		return userService.createUser(request.getBindingId(),
				FULL_ACCESS, BOOK_STORE_ID_PREFIX + request.getServiceInstanceId());
	}

	private Map<String, Object> buildCredentials(String instanceId, User user) {
		String uri = buildUri(instanceId);

		Map<String, Object> credentials = new HashMap<>();
		credentials.put(URI_KEY, uri);
		credentials.put(USERNAME_KEY, user.getUsername());
		credentials.put(PASSWORD_KEY, user.getPassword());
		return credentials;
	}

	private String buildUri(String instanceId) {
		return UriComponentsBuilder
					.fromUriString(applicationInformation.getBaseUrl())
					.pathSegment("bookstores", instanceId)
					.build()
					.toUriString();
	}

	private void saveBinding(CreateServiceInstanceBindingRequest request, Map<String, Object> credentials) {
		ServiceBinding serviceBinding =
				new ServiceBinding(request.getBindingId(), request.getParameters(), credentials);
		bindingRepository.save(serviceBinding);
	}
}