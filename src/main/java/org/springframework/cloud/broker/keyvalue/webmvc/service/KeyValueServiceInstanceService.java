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

import org.springframework.cloud.broker.keyvalue.webmvc.model.ServiceInstance;
import org.springframework.cloud.broker.keyvalue.webmvc.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.instances.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instances.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instances.CreateServiceInstanceResponse.CreateServiceInstanceResponseBuilder;
import org.springframework.cloud.servicebroker.model.instances.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instances.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instances.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.instances.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.instances.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instances.UpdateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

@Service
public class KeyValueServiceInstanceService implements ServiceInstanceService {
	private final ServiceInstanceRepository instanceRepository;
	private final KeyValueStore store;

	public KeyValueServiceInstanceService(KeyValueStore store, ServiceInstanceRepository instanceRepository) {
		this.store = store;
		this.instanceRepository = instanceRepository;
	}

	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
		String instanceId = request.getServiceInstanceId();

		CreateServiceInstanceResponseBuilder responseBuilder = CreateServiceInstanceResponse.builder();

		if (instanceRepository.existsById(instanceId)) {
			responseBuilder.instanceExisted(true);
		} else {
			store.createMap(instanceId);
			instanceRepository.save(new ServiceInstance(instanceId, request.getContext()));
		}

		return responseBuilder.build();
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
		return null;
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
		String instanceId = request.getServiceInstanceId();

		if (instanceRepository.existsById(instanceId)) {
			store.deleteMap(instanceId);
			instanceRepository.deleteById(instanceId);

			return DeleteServiceInstanceResponse.builder().build();
		} else {
			throw new ServiceInstanceDoesNotExistException(instanceId);
		}
	}

	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
		return null;
	}
}
