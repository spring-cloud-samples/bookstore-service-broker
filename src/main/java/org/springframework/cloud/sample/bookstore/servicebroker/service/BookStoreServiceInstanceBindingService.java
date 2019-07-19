/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.cloud.sample.bookstore.servicebroker.service;

import org.springframework.cloud.sample.bookstore.servicebroker.credhub.CredhubCreateServiceInstanceBinding;
import org.springframework.cloud.sample.bookstore.servicebroker.credhub.CredhubDeleteServiceInstanceBinding;
import org.springframework.cloud.sample.bookstore.servicebroker.model.ServiceBinding;
import org.springframework.cloud.sample.bookstore.servicebroker.repository.ServiceBindingRepository;
import org.springframework.cloud.sample.bookstore.web.model.ApplicationInformation;
import org.springframework.cloud.sample.bookstore.web.model.User;
import org.springframework.cloud.sample.bookstore.web.service.UserService;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.*;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse.CreateServiceInstanceAppBindingResponseBuilder;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingResponse.DeleteServiceInstanceBindingResponseBuilder;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.BOOK_STORE_ID_PREFIX;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.FULL_ACCESS;

@Service
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BookStoreServiceInstanceBindingService implements ServiceInstanceBindingService {
	private static final String URI_KEY = "uri";
	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";

	private final ServiceBindingRepository bindingRepository;
	private final UserService userService;
	private final ApplicationInformation applicationInformation;

	private final Optional<CredhubCreateServiceInstanceBinding> credhubCreate;
	private final Optional<CredhubDeleteServiceInstanceBinding> credhubDelete;

	public BookStoreServiceInstanceBindingService(ServiceBindingRepository bindingRepository,
												  UserService userService,
												  ApplicationInformation applicationInformation,
												  Optional<CredhubCreateServiceInstanceBinding> credhubCreate,
												  Optional<CredhubDeleteServiceInstanceBinding> credhubDelete) {
		this.bindingRepository = bindingRepository;
		this.userService = userService;
		this.applicationInformation = applicationInformation;
		this.credhubCreate = credhubCreate;
		this.credhubDelete = credhubDelete;
	}

	@Override
	public Mono<CreateServiceInstanceBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
		CreateServiceInstanceAppBindingResponseBuilder responseBuilder =
			CreateServiceInstanceAppBindingResponse.builder();

		Optional<ServiceBinding> bindingRecord = bindingRepository.findById(request.getBindingId());

		if (bindingRecord.isPresent()) {
			responseBuilder.bindingExisted(true).credentials(bindingRecord.get().getCredentials());
			return Mono.just(responseBuilder.build());
		} else {
			User user = createUser(request);

			Map<String, Object> credentials = buildCredentials(request.getServiceInstanceId(), user);
			responseBuilder.bindingExisted(false).credentials(credentials);

			return this.credhubCreate.map(credhub -> credhub.buildResponse(request, responseBuilder)
				.map(convertedBuilder ->
				{
					CreateServiceInstanceAppBindingResponse response = convertedBuilder.build();
					saveBinding(request, response.getCredentials());
					return (CreateServiceInstanceBindingResponse) response;
				})
			).orElseGet(() -> {
				saveBinding(request, credentials);
				return Mono.just(responseBuilder.build());
			});
		}
	}

	@Override
	public Mono<GetServiceInstanceBindingResponse> getServiceInstanceBinding(GetServiceInstanceBindingRequest request) {
		String bindingId = request.getBindingId();

		Optional<ServiceBinding> bindingRecord = bindingRepository.findById(bindingId);

		if (bindingRecord.isPresent()) {
			return Mono.just(GetServiceInstanceAppBindingResponse.builder()
				.parameters(bindingRecord.get().getParameters())
				.credentials(bindingRecord.get().getCredentials())
				.build());
		} else {
			throw new ServiceInstanceBindingDoesNotExistException(bindingId);
		}
	}

	@Override
	public Mono<DeleteServiceInstanceBindingResponse> deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
		String bindingId = request.getBindingId();
		DeleteServiceInstanceBindingResponseBuilder builder = DeleteServiceInstanceBindingResponse.builder();

		if (bindingRepository.existsById(bindingId)) {
			bindingRepository.deleteById(bindingId);
			userService.deleteUser(bindingId);
			return this.credhubDelete
				.map(credhub -> credhub.buildResponse(request, builder))
				.orElseGet(() -> Mono.just(DeleteServiceInstanceBindingResponse.builder()))
				.map(DeleteServiceInstanceBindingResponseBuilder::build);
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
