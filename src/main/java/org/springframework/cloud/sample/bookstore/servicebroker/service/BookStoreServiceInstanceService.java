/*
 * Copyright 2002-2026 the original author or authors.
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

import org.springframework.cloud.sample.bookstore.servicebroker.model.ServiceInstance;
import org.springframework.cloud.sample.bookstore.servicebroker.repository.ServiceInstanceRepository;
import org.springframework.cloud.sample.bookstore.web.service.BookStoreService;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.instance.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

@Service
public class BookStoreServiceInstanceService implements ServiceInstanceService {

	private final BookStoreService storeService;

	private final ServiceInstanceRepository instanceRepository;

	private final Map<String, Future<?>> tasks;

	public BookStoreServiceInstanceService(BookStoreService storeService,
			ServiceInstanceRepository instanceRepository) {
		this.storeService = storeService;
		this.instanceRepository = instanceRepository;
		this.tasks = Collections.synchronizedMap(new HashMap<>());
	}

	@Override
	public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
		String operationName = "provision-" + request.getServiceInstanceId();

		return Mono.just(request.getServiceInstanceId())
			.flatMap((instanceId) -> Mono.just(CreateServiceInstanceResponse.builder())
				.flatMap((responseBuilder) -> this.instanceRepository.existsById(instanceId).flatMap((exists) -> {
					if (exists) {
						return Mono.just(responseBuilder.instanceExisted(true).build());
					}

					this.tasks.putIfAbsent(operationName, ForkJoinPool.commonPool()
						.submit(() -> this.storeService.createBookStore(instanceId)
							.then(this.instanceRepository.save(new ServiceInstance(instanceId,
									request.getServiceDefinitionId(), request.getPlanId(), request.getParameters())))
							.block()));
					return Mono.just(responseBuilder.async(true).operation(operationName).build());
				})));
	}

	@Override
	public Mono<GetServiceInstanceResponse> getServiceInstance(GetServiceInstanceRequest request) {
		return Mono.just(request.getServiceInstanceId())
			.flatMap((instanceId) -> this.instanceRepository.findById(instanceId)
				.switchIfEmpty(Mono.error(new ServiceInstanceDoesNotExistException(instanceId)))
				.flatMap((serviceInstance) -> Mono.just(GetServiceInstanceResponse.builder()
					.serviceDefinitionId(serviceInstance.getServiceDefinitionId())
					.planId(serviceInstance.getPlanId())
					.parameters(serviceInstance.getParameters())
					.build())));
	}

	@Override
	public Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {
		String operationName = "deprovision-" + request.getServiceInstanceId();

		return Mono.just(request.getServiceInstanceId())
			.flatMap((instanceId) -> this.instanceRepository.existsById(instanceId).flatMap((exists) -> {
				if (!exists) {
					return Mono.error(new ServiceInstanceDoesNotExistException(instanceId));
				}

				this.tasks.putIfAbsent(operationName,
						ForkJoinPool.commonPool()
							.submit(() -> this.storeService.deleteBookStore(instanceId)
								.then(this.instanceRepository.deleteById(instanceId))
								.block()));
				return Mono.just(DeleteServiceInstanceResponse.builder().async(true).operation(operationName).build());
			}));
	}

	@Override
	public Mono<GetLastServiceOperationResponse> getLastOperation(GetLastServiceOperationRequest request) {
		return Mono.just(request.getOperation()).flatMap((operationName) -> {
			Future<?> task = tasks.get(operationName);
			if (task == null) {
				return Mono.error(new ServiceBrokerInvalidParametersException("unknown operation: " + operationName));
			}

			if (task.isDone()) {
				try {
					task.get();
				}
				catch (ExecutionException | InterruptedException e) {
					return Mono.just(GetLastServiceOperationResponse.builder()
						.operationState(OperationState.FAILED)
						.description(e.getMessage())
						.build());
				}
				finally {
					tasks.remove(operationName);
				}

				return Mono
					.just(GetLastServiceOperationResponse.builder().operationState(OperationState.SUCCEEDED).build());
			}

			return Mono
				.just(GetLastServiceOperationResponse.builder().operationState(OperationState.IN_PROGRESS).build());
		});
	}

}
