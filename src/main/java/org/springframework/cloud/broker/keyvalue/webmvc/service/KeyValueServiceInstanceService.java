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

import org.springframework.cloud.broker.keyvalue.webmvc.repository.ServiceInstanceRepository;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

@Service
public class KeyValueServiceInstanceService implements ServiceInstanceService {
	private ServiceInstanceRepository instanceRepository;
	private KeyValueStore store;

	public KeyValueServiceInstanceService(KeyValueStore store, ServiceInstanceRepository instanceRepository) {
		this.store = store;
		this.instanceRepository = instanceRepository;
	}

	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
		String instanceId = request.getServiceInstanceId();
		String definitionId = request.getServiceDefinitionId();

		if (instanceRepository.existsById(instanceId)) {
			throw new ServiceInstanceExistsException(instanceId, definitionId);
		}

		store.createInstance(instanceId);

		return new CreateServiceInstanceResponse()
				.withInstanceExisted(false);
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
		return null;
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
		String instanceId = request.getServiceInstanceId();

		if (instanceRepository.existsById(instanceId)) {
			store.deleteInstance(instanceId);
			instanceRepository.deleteById(instanceId);

			return new DeleteServiceInstanceResponse();
		} else {
			throw new ServiceInstanceDoesNotExistException(instanceId);
		}
	}

	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
		return null;
	}
}
