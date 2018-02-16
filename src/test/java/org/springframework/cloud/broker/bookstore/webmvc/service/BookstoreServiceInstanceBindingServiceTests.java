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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.cloud.broker.bookstore.webmvc.model.ApplicationInformation;
import org.springframework.cloud.broker.bookstore.webmvc.model.ServiceBinding;
import org.springframework.cloud.broker.bookstore.webmvc.repository.ServiceBindingRepository;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.model.Context;
import org.springframework.cloud.servicebroker.model.bindings.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.bindings.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.bindings.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.bindings.DeleteServiceInstanceBindingRequest;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BookstoreServiceInstanceBindingServiceTests {
	private static final String SERVICE_INSTANCE_ID = "instance-id";
	private static final String SERVICE_BINDING_ID = "binding-id";
	private static final String BASE_URL = "https://localhost:8080";

	@Mock
	private ServiceBindingRepository repository;

	private BookStoreServiceInstanceBindingService service;

	@Before
	public void setUp() {
		initMocks(this);

		ApplicationInformation appInfo = new ApplicationInformation(BASE_URL);

		User user = new User("testuser", "testpassword", Collections.emptyList());

		service = new BookStoreServiceInstanceBindingService(repository, appInfo, user);
	}
	
	@Test
	public void createBindingWhenBindingDoesNotExist() {
		when(repository.findById(SERVICE_BINDING_ID))
				.thenReturn(Optional.empty());

		CreateServiceInstanceBindingRequest request = CreateServiceInstanceBindingRequest.builder()
				.serviceInstanceId(SERVICE_INSTANCE_ID)
				.bindingId(SERVICE_BINDING_ID)
				.build();

		CreateServiceInstanceBindingResponse response = service.createServiceInstanceBinding(request);

		assertThat(response).isInstanceOf(CreateServiceInstanceAppBindingResponse.class);

		CreateServiceInstanceAppBindingResponse appResponse = (CreateServiceInstanceAppBindingResponse) response;
		assertThat(appResponse.isBindingExisted()).isFalse();

		Map<String, Object> credentials = appResponse.getCredentials();
		assertThat(credentials)
				.hasSize(3)
				.containsOnlyKeys("uri", "username", "password");

		assertThat(credentials.get("uri").toString())
			.startsWith(BASE_URL)
			.endsWith("bookstore/" + SERVICE_INSTANCE_ID);

		assertThat(credentials.get("username").toString()).isEqualTo("testuser");
		assertThat(credentials.get("password").toString()).isEqualTo("testpassword");

		verify(repository).findById(SERVICE_BINDING_ID);

		ArgumentCaptor<ServiceBinding> argumentCaptor = ArgumentCaptor.forClass(ServiceBinding.class);
		verify(repository).save(argumentCaptor.capture());
		ServiceBinding actual = argumentCaptor.getValue();
		assertThat(actual.getBindingId()).isEqualTo(SERVICE_BINDING_ID);
		assertThat(actual.getCredentials()).isEqualTo(credentials);

		verifyNoMoreInteractions(repository);
	}

	@Test
	public void createBindingWhenBindingExists() {
		Map<String, Object> credentials = new HashMap<String, Object>() {{
			put("uri", "http://example.com");
			put("username", "testuser");
			put("password", "testpassword");
		}};
		ServiceBinding binding = new ServiceBinding(SERVICE_BINDING_ID, Context.builder().build(), credentials);
		when(repository.findById(SERVICE_BINDING_ID))
				.thenReturn(Optional.of(binding));

		CreateServiceInstanceBindingRequest request = CreateServiceInstanceBindingRequest.builder()
				.serviceInstanceId(SERVICE_INSTANCE_ID)
				.bindingId(SERVICE_BINDING_ID)
				.build();

		CreateServiceInstanceBindingResponse response = service.createServiceInstanceBinding(request);

		assertThat(response).isInstanceOf(CreateServiceInstanceAppBindingResponse.class);

		CreateServiceInstanceAppBindingResponse appResponse = (CreateServiceInstanceAppBindingResponse) response;
		assertThat(appResponse.isBindingExisted()).isTrue();

		assertThat(credentials).isEqualTo(appResponse.getCredentials());

		verify(repository).findById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void deleteBindingWhenBindingExists() {
		when(repository.existsById(SERVICE_BINDING_ID))
				.thenReturn(true);

		DeleteServiceInstanceBindingRequest request = DeleteServiceInstanceBindingRequest.builder()
				.serviceInstanceId(SERVICE_INSTANCE_ID)
				.bindingId(SERVICE_BINDING_ID)
				.build();

		service.deleteServiceInstanceBinding(request);

		verify(repository).existsById(SERVICE_BINDING_ID);
		verify(repository).deleteById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);
	}

	@Test(expected = ServiceInstanceBindingDoesNotExistException.class)
	public void deleteBindingWhenBindingDoesNotExist() {
		when(repository.existsById(SERVICE_BINDING_ID))
				.thenReturn(false);

		DeleteServiceInstanceBindingRequest request = DeleteServiceInstanceBindingRequest.builder()
				.serviceInstanceId(SERVICE_INSTANCE_ID)
				.bindingId(SERVICE_BINDING_ID)
				.build();

		service.deleteServiceInstanceBinding(request);
	}
}