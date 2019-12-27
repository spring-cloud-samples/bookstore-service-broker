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

package org.springframework.cloud.sample.bookstore.servicebroker.service;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.cloud.sample.bookstore.servicebroker.model.ServiceBinding;
import org.springframework.cloud.sample.bookstore.servicebroker.repository.ServiceBindingRepository;
import org.springframework.cloud.sample.bookstore.web.model.ApplicationInformation;
import org.springframework.cloud.sample.bookstore.web.model.User;
import org.springframework.cloud.sample.bookstore.web.service.UserService;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.GetServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.GetServiceInstanceBindingRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.BOOK_STORE_ID_PREFIX;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.FULL_ACCESS;

public class BookstoreServiceInstanceBindingServiceTests {

	private static final String SERVICE_INSTANCE_ID = "instance-id";

	private static final String SERVICE_BINDING_ID = "binding-id";

	private static final String BASE_URL = "https://localhost:8080";

	@Mock
	private ServiceBindingRepository repository;

	@Mock
	private UserService userService;

	private BookStoreServiceInstanceBindingService service;

	private Map<String, Object> credentials;

	@BeforeEach
	public void setUp() {
		this.credentials = new HashMap<>();
		credentials.put("uri", "https://example.com");
		credentials.put("username", "testuser");
		credentials.put("password", "testpassword");

		initMocks(this);

		ApplicationInformation appInfo = new ApplicationInformation(BASE_URL);

		service = new BookStoreServiceInstanceBindingService(repository, userService, appInfo);
	}

	@Test
	public void createBindingWhenBindingDoesNotExist() {
		when(repository.existsById(SERVICE_BINDING_ID))
				.thenReturn(Mono.just(false));

		when(userService.createUser(eq(SERVICE_BINDING_ID), eq(FULL_ACCESS),
				eq(BOOK_STORE_ID_PREFIX + SERVICE_INSTANCE_ID)))
				.thenReturn(Mono.just(new User(SERVICE_BINDING_ID, "password", FULL_ACCESS,
						BOOK_STORE_ID_PREFIX + SERVICE_INSTANCE_ID)));

		final Map<String, Object> creds = new HashMap<>();
		creds.put("uri", BASE_URL + "/bookstores/" + SERVICE_INSTANCE_ID);
		creds.put("username", SERVICE_BINDING_ID);
		creds.put("password", "password");

		ServiceBinding binding = new ServiceBinding(SERVICE_BINDING_ID, new HashMap<>(), creds);
		when(repository.save(refEq(binding)))
				.thenReturn(Mono.just(binding));

		CreateServiceInstanceBindingRequest request = CreateServiceInstanceBindingRequest.builder()
				.serviceInstanceId(SERVICE_INSTANCE_ID)
				.bindingId(SERVICE_BINDING_ID)
				.build();

		StepVerifier.create(service.createServiceInstanceBinding(request))
				.consumeNextWith(response -> {
					assertThat(response).isInstanceOf(CreateServiceInstanceAppBindingResponse.class);
					CreateServiceInstanceAppBindingResponse appResponse = (CreateServiceInstanceAppBindingResponse) response;
					assertThat(appResponse.isBindingExisted()).isFalse();
					Map<String, Object> credentials = appResponse.getCredentials();
					assertThat(credentials)
							.hasSize(3)
							.containsOnlyKeys("uri", "username", "password");
					assertThat(credentials.get("uri").toString())
							.startsWith(BASE_URL)
							.endsWith("bookstores/" + SERVICE_INSTANCE_ID);

					ArgumentCaptor<ServiceBinding> repositoryCaptor = ArgumentCaptor.forClass(ServiceBinding.class);
					verify(repository).save(repositoryCaptor.capture());
					ServiceBinding actualBinding = repositoryCaptor.getValue();
					assertThat(actualBinding.getBindingId()).isEqualTo(SERVICE_BINDING_ID);
					assertThat(actualBinding.getCredentials()).isEqualTo(credentials);
				})
				.verifyComplete();

		verify(repository).existsById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);

		verify(userService).createUser(SERVICE_BINDING_ID, FULL_ACCESS, BOOK_STORE_ID_PREFIX + SERVICE_INSTANCE_ID);
		verify(repository).save(refEq(binding));
		verifyNoMoreInteractions(userService);
	}

	@Test
	public void createBindingWhenBindingExists() {
		ServiceBinding binding = new ServiceBinding(SERVICE_BINDING_ID, null, credentials);

		when(repository.existsById(SERVICE_BINDING_ID))
				.thenReturn(Mono.just(true));

		when(repository.findById(SERVICE_BINDING_ID))
				.thenReturn(Mono.just(binding));

		CreateServiceInstanceBindingRequest request = CreateServiceInstanceBindingRequest.builder()
				.serviceInstanceId(SERVICE_INSTANCE_ID)
				.bindingId(SERVICE_BINDING_ID)
				.build();

		StepVerifier.create(service.createServiceInstanceBinding(request))
				.consumeNextWith(response -> {
					assertThat(response).isInstanceOf(CreateServiceInstanceAppBindingResponse.class);
					CreateServiceInstanceAppBindingResponse appResponse = (CreateServiceInstanceAppBindingResponse) response;
					assertThat(appResponse.isBindingExisted()).isTrue();
					assertThat(credentials).isEqualTo(appResponse.getCredentials());
				})
				.verifyComplete();

		verify(repository).existsById(SERVICE_BINDING_ID);
		verify(repository).findById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void getBindingWhenBindingExists() {
		HashMap<String, Object> parameters = new HashMap<>();
		ServiceBinding serviceBinding = new ServiceBinding(SERVICE_BINDING_ID, parameters, credentials);

		when(repository.findById(SERVICE_BINDING_ID))
				.thenReturn(Mono.just(serviceBinding));

		GetServiceInstanceBindingRequest request = GetServiceInstanceBindingRequest.builder()
				.bindingId(SERVICE_BINDING_ID)
				.build();

		StepVerifier.create(service.getServiceInstanceBinding(request))
				.consumeNextWith(response -> {
					assertThat(response).isInstanceOf(GetServiceInstanceAppBindingResponse.class);
					GetServiceInstanceAppBindingResponse appResponse = (GetServiceInstanceAppBindingResponse) response;
					assertThat(appResponse.getParameters()).isEqualTo(parameters);
					assertThat(appResponse.getCredentials()).isEqualTo(credentials);
				})
				.verifyComplete();

		verify(repository).findById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void getBindingWhenBindingDoesNotExist() {
		when(repository.findById(SERVICE_BINDING_ID))
				.thenReturn(Mono.empty());

		GetServiceInstanceBindingRequest request = GetServiceInstanceBindingRequest.builder()
				.bindingId(SERVICE_BINDING_ID)
				.build();

		StepVerifier.create(service.getServiceInstanceBinding(request))
				.expectErrorMatches(e -> e instanceof ServiceInstanceBindingDoesNotExistException)
				.verify();

		verify(repository).findById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void deleteBindingWhenBindingExists() {
		when(repository.existsById(SERVICE_BINDING_ID))
				.thenReturn(Mono.just(true));

		when(repository.deleteById(SERVICE_BINDING_ID))
				.thenReturn(Mono.empty());

		when(userService.deleteUser(SERVICE_BINDING_ID))
				.thenReturn(Mono.empty());

		DeleteServiceInstanceBindingRequest request = DeleteServiceInstanceBindingRequest.builder()
				.serviceInstanceId(SERVICE_INSTANCE_ID)
				.bindingId(SERVICE_BINDING_ID)
				.build();

		StepVerifier.create(service.deleteServiceInstanceBinding(request))
				.expectNextCount(1)
				.verifyComplete();

		verify(repository).existsById(SERVICE_BINDING_ID);
		verify(repository).deleteById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);

		verify(userService).deleteUser(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(userService);
	}

	@Test
	public void deleteBindingWhenBindingDoesNotExist() {
		when(repository.existsById(SERVICE_BINDING_ID))
				.thenReturn(Mono.just(false));

		DeleteServiceInstanceBindingRequest request = DeleteServiceInstanceBindingRequest.builder()
				.serviceInstanceId(SERVICE_INSTANCE_ID)
				.bindingId(SERVICE_BINDING_ID)
				.build();

		StepVerifier.create(service.deleteServiceInstanceBinding(request))
				.expectErrorMatches(e -> e instanceof ServiceInstanceBindingDoesNotExistException)
				.verify();

		verify(repository).existsById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);
	}

}
