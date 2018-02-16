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

package org.springframework.cloud.broker.bookstore.webmvc.model;


import org.springframework.cloud.servicebroker.model.Context;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.util.Map;

@KeySpace("serviceBindings")
public class ServiceBinding {
	@Id
	private final String bindingId;

	private final Context context;

	private final Map<String, Object> credentials;

	public ServiceBinding(String bindingId, Context context, Map<String, Object> credentials) {
		this.bindingId = bindingId;
		this.context = context;
		this.credentials = credentials;
	}

	public String getBindingId() {
		return bindingId;
	}

	public Context getContext() {
		return context;
	}

	public Map<String, Object> getCredentials() {
		return credentials;
	}
}
