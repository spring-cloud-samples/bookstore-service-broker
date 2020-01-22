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

package org.springframework.cloud.sample.bookstore.servicebroker.model;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ServiceInstance {

	@Id
	private String instanceId;

	private String serviceDefinitionId;

	private String planId;

	private Map<String, Object> parameters;

	@SuppressWarnings("unused")
	private ServiceInstance() {
		instanceId = null;
		serviceDefinitionId = null;
		planId = null;
		parameters = null;
	}

	public ServiceInstance(String instanceId, String serviceDefinitionId, String planId,
			Map<String, Object> parameters) {
		this.instanceId = instanceId;
		this.serviceDefinitionId = serviceDefinitionId;
		this.planId = planId;
		this.parameters = parameters;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public String getServiceDefinitionId() {
		return serviceDefinitionId;
	}

	public String getPlanId() {
		return planId;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

}
