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
@Table(name = "service_bindings")
public class ServiceBinding {
	@Id
	@Column(length = 50)
	private final String bindingId;

	@ElementCollection
	@MapKeyColumn(name = "parameter_name", length = 100)
	@Column(name = "parameter_value")
	@CollectionTable(name = "service_binding_parameters", joinColumns = @JoinColumn(name = "binding_id"))
	@Convert(converter = ObjectToStringConverter.class, attributeName = "value")
	private final Map<String, Object> parameters;

	@ElementCollection
	@MapKeyColumn(name = "credential_name", length = 100)
	@Column(name = "credential_value")
	@CollectionTable(name = "service_binding_credentials", joinColumns = @JoinColumn(name = "binding_id"))
	@Convert(converter = ObjectToStringConverter.class, attributeName = "value")
	private final Map<String, Object> credentials;

	@SuppressWarnings("unused")
	private ServiceBinding() {
		this.bindingId = null;
		this.parameters = null;
		this.credentials = null;
	}

	public ServiceBinding(String bindingId, Map<String, Object> parameters, Map<String, Object> credentials) {
		this.bindingId = bindingId;
		this.parameters = parameters;
		this.credentials = credentials;
	}

	public String getBindingId() {
		return bindingId;
	}

	public Map<String, Object> getCredentials() {
		return credentials;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}
}
