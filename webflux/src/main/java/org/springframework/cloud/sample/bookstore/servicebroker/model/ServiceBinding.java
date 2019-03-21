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

package org.springframework.cloud.sample.bookstore.servicebroker.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document
public class ServiceBinding {
	@Id
	private final String bindingId;

	private final Map<String, Object> parameters;
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
