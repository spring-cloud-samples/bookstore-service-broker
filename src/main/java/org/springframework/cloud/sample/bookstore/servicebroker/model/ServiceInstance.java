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

package org.springframework.cloud.sample.bookstore.servicebroker.model;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import java.util.Map;

@Entity
@Table(name = "service_instances")
public class ServiceInstance {
	@Id
	@Column(length = 50)
	private final String instanceId;

	@Column(length = 50)
	private final String serviceDefinitionId;

	@Column(length = 50)
	private final String planId;

	@ElementCollection
	@MapKeyColumn(name="parameter_name", length = 100)
	@Column(name = "parameter_value")
	@CollectionTable(name="service_instance_parameters", joinColumns = @JoinColumn(name = "instance_id"))
	@Convert(converter = ObjectToStringConverter.class, attributeName = "value")
	private final Map<String, Object> parameters;

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
