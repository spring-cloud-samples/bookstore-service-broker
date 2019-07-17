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

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.cloud.sample.bookstore.servicebroker.credhub.CredhubCreateServiceInstanceBinding;
import org.springframework.cloud.sample.bookstore.servicebroker.credhub.CredhubDeleteServiceInstanceBinding;
import org.springframework.cloud.sample.bookstore.servicebroker.model.ServiceBinding;
import org.springframework.cloud.sample.bookstore.servicebroker.repository.ServiceBindingRepository;
import org.springframework.cloud.sample.bookstore.web.model.ApplicationInformation;
import org.springframework.cloud.sample.bookstore.web.model.User;
import org.springframework.cloud.sample.bookstore.web.service.UserService;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.*;
import org.springframework.credhub.core.CredHubOperations;
import org.springframework.credhub.core.credential.CredHubCredentialOperations;
import org.springframework.credhub.core.permissionV2.CredHubPermissionV2Operations;
import org.springframework.credhub.support.CredentialDetails;
import org.springframework.credhub.support.CredentialName;
import org.springframework.credhub.support.ServiceInstanceCredentialName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.BOOK_STORE_ID_PREFIX;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.FULL_ACCESS;

public class BookstoreServiceInstanceBindingServiceTests {
	private static final String APP_NAME = "test-app";
	private static final String SERVICE_INSTANCE_ID = "instance-id";
	private static final String SERVICE_BINDING_ID = "binding-id";
	private static final String BASE_URL = "https://localhost:8080";
	private static final String SERVICE_DEFINITION_ID = "service-definition-id";
	private static final String CREDENTIAL_NAME = "credentials-json";

	@Mock
	private ServiceBindingRepository repository;

	@Mock
	private UserService userService;

	@Mock
	private CredHubOperations credHubOperations;

	@Mock
	private CredHubCredentialOperations credHubCredentialOperations;

	@Mock
	private CredHubPermissionV2Operations credHubPermissionV2Operations;

	private BookStoreServiceInstanceBindingService service;
	private BookStoreServiceInstanceBindingService credHubEnabledService;

	private final Map<String, Object> credentials = new HashMap<String, Object>() {{
		put("uri", "https://example.com");
		put("username", "testuser");
		put("password", "testpassword");
	}};

	@Before
	public void setUp() {
		initMocks(this);
		when(credHubOperations.credentials()).thenReturn(credHubCredentialOperations);
		when(credHubOperations.permissionsV2()).thenReturn(credHubPermissionV2Operations);

		ApplicationInformation appInfo = new ApplicationInformation(BASE_URL);

		service = new BookStoreServiceInstanceBindingService(repository, userService, appInfo, Optional.empty(), Optional.empty());

		CredhubCreateServiceInstanceBinding credHubCreate = new CredhubCreateServiceInstanceBinding(credHubOperations, APP_NAME);
		CredhubDeleteServiceInstanceBinding credHubDelete = new CredhubDeleteServiceInstanceBinding(credHubOperations, APP_NAME);
		credHubEnabledService = new BookStoreServiceInstanceBindingService(repository, userService, appInfo, Optional.of(credHubCreate), Optional.of(credHubDelete));
	}

	@Test
	public void createBindingWhenBindingDoesNotExist() {
		when(repository.findById(SERVICE_BINDING_ID))
			.thenReturn(Optional.empty());

		when(userService.createUser(SERVICE_BINDING_ID, FULL_ACCESS, BOOK_STORE_ID_PREFIX + SERVICE_INSTANCE_ID))
			.thenReturn(new User(SERVICE_BINDING_ID, "password", FULL_ACCESS, BOOK_STORE_ID_PREFIX + SERVICE_INSTANCE_ID));

		CreateServiceInstanceBindingRequest request = CreateServiceInstanceBindingRequest.builder()
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.bindingId(SERVICE_BINDING_ID)
			.build();

		CreateServiceInstanceBindingResponse response = service.createServiceInstanceBinding(request).block();

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

		verify(repository).findById(SERVICE_BINDING_ID);

		ArgumentCaptor<ServiceBinding> repositoryCaptor = ArgumentCaptor.forClass(ServiceBinding.class);
		verify(repository).save(repositoryCaptor.capture());
		ServiceBinding actualBinding = repositoryCaptor.getValue();
		assertThat(actualBinding.getBindingId()).isEqualTo(SERVICE_BINDING_ID);
		assertThat(actualBinding.getCredentials()).isEqualTo(credentials);

		verifyNoMoreInteractions(repository);
	}

	@Test
	public void createBindingWhenBindingExists() {
		ServiceBinding binding = new ServiceBinding(SERVICE_BINDING_ID, null, credentials);

		when(repository.findById(SERVICE_BINDING_ID))
			.thenReturn(Optional.of(binding));

		CreateServiceInstanceBindingRequest request = CreateServiceInstanceBindingRequest.builder()
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.bindingId(SERVICE_BINDING_ID)
			.build();

		CreateServiceInstanceBindingResponse response = service.createServiceInstanceBinding(request).block();

		assertThat(response).isInstanceOf(CreateServiceInstanceAppBindingResponse.class);

		CreateServiceInstanceAppBindingResponse appResponse = (CreateServiceInstanceAppBindingResponse) response;
		assertThat(appResponse.isBindingExisted()).isTrue();

		assertThat(credentials).isEqualTo(appResponse.getCredentials());

		verify(repository).findById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void createBindingWithCredhubWhenBindingDoesNotExist() {
		when(repository.findById(SERVICE_BINDING_ID)).thenReturn(Optional.empty());

		when(userService.createUser(SERVICE_BINDING_ID, FULL_ACCESS, BOOK_STORE_ID_PREFIX + SERVICE_INSTANCE_ID))
			.thenReturn(new User(SERVICE_BINDING_ID, "password", FULL_ACCESS, BOOK_STORE_ID_PREFIX + SERVICE_INSTANCE_ID));

		CreateServiceInstanceBindingRequest request = CreateServiceInstanceBindingRequest.builder()
			.serviceDefinitionId(SERVICE_DEFINITION_ID)
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.bindingId(SERVICE_BINDING_ID)
			.bindResource(BindResource.builder().appGuid("app-guid").properties("credential_client_id", "moo").build())
			.build();

		CreateServiceInstanceBindingResponse response = credHubEnabledService.createServiceInstanceBinding(request).block();

		assertThat(response).isInstanceOf(CreateServiceInstanceAppBindingResponse.class);

		CreateServiceInstanceAppBindingResponse appResponse = (CreateServiceInstanceAppBindingResponse) response;
		assertThat(appResponse.isBindingExisted()).isFalse();

		Map<String, Object> credentials = appResponse.getCredentials();

		assertThat(credentials).hasSize(1).containsOnlyKeys("credhub-ref");
		assertThat(credentials.get("credhub-ref").toString()).isEqualTo("/c/" + APP_NAME + "/" + SERVICE_DEFINITION_ID + "/" + SERVICE_BINDING_ID + "/" + CREDENTIAL_NAME);

		verify(repository).findById(SERVICE_BINDING_ID);
		verify(credHubOperations, times(1)).credentials();
		verify(credHubOperations, times(2)).permissionsV2();

		ArgumentCaptor<ServiceBinding> repositoryCaptor = ArgumentCaptor.forClass(ServiceBinding.class);
		verify(repository).save(repositoryCaptor.capture());
		ServiceBinding actualBinding = repositoryCaptor.getValue();
		assertThat(actualBinding.getBindingId()).isEqualTo(SERVICE_BINDING_ID);
		assertThat(actualBinding.getCredentials()).isEqualTo(credentials);

		verifyNoMoreInteractions(repository);
	}

	@Test
	public void getBindingWhenBindingExists() {
		HashMap<String, Object> parameters = new HashMap<>();
		ServiceBinding serviceBinding = new ServiceBinding(SERVICE_BINDING_ID, parameters, credentials);

		when(repository.findById(SERVICE_BINDING_ID))
			.thenReturn(Optional.of(serviceBinding));

		GetServiceInstanceBindingRequest request = GetServiceInstanceBindingRequest.builder()
			.bindingId(SERVICE_BINDING_ID)
			.build();

		GetServiceInstanceBindingResponse response = service.getServiceInstanceBinding(request).block();

		assertThat(response).isInstanceOf(GetServiceInstanceAppBindingResponse.class);

		GetServiceInstanceAppBindingResponse appResponse = (GetServiceInstanceAppBindingResponse) response;

		assertThat(appResponse.getParameters()).isEqualTo(parameters);
		assertThat(appResponse.getCredentials()).isEqualTo(credentials);

		verify(repository).findById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);
	}

	@Test(expected = ServiceInstanceBindingDoesNotExistException.class)
	public void getBindingWhenBindingDoesNotExist() {
		when(repository.findById(SERVICE_BINDING_ID))
			.thenReturn(Optional.empty());

		GetServiceInstanceBindingRequest request = GetServiceInstanceBindingRequest.builder()
			.bindingId(SERVICE_BINDING_ID)
			.build();

		service.getServiceInstanceBinding(request).block();
	}

	@Test
	public void deleteBindingWhenBindingExists() {
		when(repository.existsById(SERVICE_BINDING_ID))
			.thenReturn(true);

		DeleteServiceInstanceBindingRequest request = DeleteServiceInstanceBindingRequest.builder()
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.bindingId(SERVICE_BINDING_ID)
			.build();

		service.deleteServiceInstanceBinding(request).block();

		verify(repository).existsById(SERVICE_BINDING_ID);
		verify(repository).deleteById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);

		verify(userService).deleteUser(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(userService);
	}

	@Test
	public void deleteBindingWithCredHubWhenBindingExists() {
		when(repository.existsById(SERVICE_BINDING_ID)).thenReturn(true);
		when(credHubCredentialOperations.findByName(any())).thenReturn(Lists.list(new CredentialDetails<>()));

		DeleteServiceInstanceBindingRequest request = DeleteServiceInstanceBindingRequest.builder()
			.serviceDefinitionId(SERVICE_DEFINITION_ID)
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.bindingId(SERVICE_BINDING_ID)
			.build();

		CredentialName name = ServiceInstanceCredentialName.builder()
			.serviceBrokerName(APP_NAME)
			.serviceOfferingName(SERVICE_DEFINITION_ID)
			.serviceBindingId(SERVICE_BINDING_ID)
			.credentialName(CREDENTIAL_NAME)
			.build();

		credHubEnabledService.deleteServiceInstanceBinding(request).block();

		verify(credHubOperations, times(2)).credentials();
		verify(credHubCredentialOperations, times(1)).findByName(name);
		verify(credHubCredentialOperations, times(1)).deleteByName(name);

		verify(repository).existsById(SERVICE_BINDING_ID);
		verify(repository).deleteById(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(repository);

		verify(userService).deleteUser(SERVICE_BINDING_ID);
		verifyNoMoreInteractions(userService);
	}

	@Test(expected = ServiceInstanceBindingDoesNotExistException.class)
	public void deleteBindingWhenBindingDoesNotExist() {
		when(repository.existsById(SERVICE_BINDING_ID))
			.thenReturn(false);

		DeleteServiceInstanceBindingRequest request = DeleteServiceInstanceBindingRequest.builder()
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.bindingId(SERVICE_BINDING_ID)
			.build();

		service.deleteServiceInstanceBinding(request).block();
	}
}
