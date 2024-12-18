/*
 * Copyright 2002-2020 the original author or authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.cloud.sample.bookstore.servicebroker.model.ServiceInstance;
import org.springframework.cloud.sample.bookstore.servicebroker.repository.ServiceInstanceRepository;
import org.springframework.cloud.sample.bookstore.web.model.BookStore;
import org.springframework.cloud.sample.bookstore.web.service.BookStoreService;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.Context;
import org.springframework.cloud.servicebroker.model.PlatformContext;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.GetServiceInstanceRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.openMocks;

public class BookstoreServiceInstanceServiceTests {

	private static final String SERVICE_INSTANCE_ID = "instance-id";

	@Mock
	private BookStoreService store;

	@Mock
	private ServiceInstanceRepository repository;

	private BookStoreServiceInstanceService service;

	@BeforeEach
	public void setUp() {
		openMocks(this);
		this.service = new BookStoreServiceInstanceService(this.store, this.repository);
	}

	@Test
	public void createServiceInstanceWhenInstanceExists() {
		given(this.repository.existsById(SERVICE_INSTANCE_ID)).willReturn(Mono.just(true));

		CreateServiceInstanceRequest request = CreateServiceInstanceRequest.builder()
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.build();

		StepVerifier.create(this.service.createServiceInstance(request)).consumeNextWith((response) -> {
			assertThat(response.isInstanceExisted()).isTrue();
			assertThat(response.getDashboardUrl()).isNull();
			assertThat(response.isAsync()).isFalse();
			assertThat(response.getOperation()).isNull();
		}).verifyComplete();

		verify(this.repository).existsById(SERVICE_INSTANCE_ID);
		verifyNoMoreInteractions(this.repository);
		verifyNoMoreInteractions(this.store);
	}

	@Test
	public void createServiceInstanceWhenInstanceDoesNotExist() {
		Context context = PlatformContext.builder().platform("test-platform").build();

		given(this.repository.existsById(SERVICE_INSTANCE_ID)).willReturn(Mono.just(false));

		given(this.store.createBookStore(SERVICE_INSTANCE_ID))
			.willReturn(Mono.just(new BookStore(SERVICE_INSTANCE_ID)));

		ServiceInstance serviceInstance = new ServiceInstance(SERVICE_INSTANCE_ID, null, null, new HashMap<>());
		given(this.repository.save(refEq(serviceInstance))).willReturn(Mono.just(serviceInstance));

		CreateServiceInstanceRequest request = CreateServiceInstanceRequest.builder()
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.context(context)
			.build();

		StepVerifier.create(this.service.createServiceInstance(request)).consumeNextWith((response) -> {
			assertThat(response.isInstanceExisted()).isFalse();
			assertThat(response.getDashboardUrl()).isNull();
			assertThat(response.isAsync()).isFalse();
			assertThat(response.getOperation()).isNull();
		}).verifyComplete();

		verify(this.repository).existsById(SERVICE_INSTANCE_ID);
		ArgumentCaptor<ServiceInstance> argumentCaptor = ArgumentCaptor.forClass(ServiceInstance.class);
		verify(this.repository).save(argumentCaptor.capture());
		verifyNoMoreInteractions(this.repository);

		ServiceInstance actual = argumentCaptor.getValue();
		assertThat(actual.getInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);

		verify(this.store).createBookStore(SERVICE_INSTANCE_ID);
		verifyNoMoreInteractions(this.store);
	}

	@Test
	public void getServiceInstanceWhenInstanceExists() {
		ServiceInstance serviceInstance = new ServiceInstance(SERVICE_INSTANCE_ID, "service-definition-id", "plan-id",
				new HashMap<>());

		given(this.repository.findById(SERVICE_INSTANCE_ID)).willReturn(Mono.just(serviceInstance));

		GetServiceInstanceRequest request = GetServiceInstanceRequest.builder()
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.build();

		StepVerifier.create(this.service.getServiceInstance(request)).consumeNextWith((response) -> {
			assertThat(response.getServiceDefinitionId()).isEqualTo(serviceInstance.getServiceDefinitionId());
			assertThat(response.getPlanId()).isEqualTo(serviceInstance.getPlanId());
			assertThat(response.getParameters()).isEqualTo(serviceInstance.getParameters());
		}).verifyComplete();

		verify(this.repository).findById(SERVICE_INSTANCE_ID);
		verifyNoMoreInteractions(this.repository);
	}

	@Test
	public void getServiceInstanceWhenInstanceDoesNotExists() {
		given(this.repository.findById(SERVICE_INSTANCE_ID)).willReturn(Mono.empty());

		GetServiceInstanceRequest request = GetServiceInstanceRequest.builder()
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.build();

		StepVerifier.create(this.service.getServiceInstance(request))
			.expectErrorMatches((e) -> e instanceof ServiceInstanceDoesNotExistException)
			.verify();
	}

	@Test
	public void deleteServiceInstanceWhenInstanceExists() {
		given(this.repository.existsById(SERVICE_INSTANCE_ID)).willReturn(Mono.just(true));

		given(this.store.deleteBookStore(SERVICE_INSTANCE_ID)).willReturn(Mono.empty());

		given(this.repository.deleteById(SERVICE_INSTANCE_ID)).willReturn(Mono.empty());

		DeleteServiceInstanceRequest request = DeleteServiceInstanceRequest.builder()
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.build();

		StepVerifier.create(this.service.deleteServiceInstance(request)).consumeNextWith((response) -> {
			assertThat(response.isAsync()).isFalse();
			assertThat(response.getOperation()).isNull();
		}).verifyComplete();

		verify(this.repository).existsById(SERVICE_INSTANCE_ID);
		verify(this.repository).deleteById(SERVICE_INSTANCE_ID);
		verifyNoMoreInteractions(this.repository);

		verify(this.store).deleteBookStore(SERVICE_INSTANCE_ID);
		verifyNoMoreInteractions(this.store);
	}

	@Test
	public void deleteServiceInstanceWhenInstanceDoesNotExist() {
		given(this.repository.existsById(SERVICE_INSTANCE_ID)).willReturn(Mono.just(false));

		DeleteServiceInstanceRequest request = DeleteServiceInstanceRequest.builder()
			.serviceInstanceId(SERVICE_INSTANCE_ID)
			.build();

		StepVerifier.create(this.service.deleteServiceInstance(request))
			.expectErrorMatches((e) -> e instanceof ServiceInstanceDoesNotExistException)
			.verify();
	}

}
