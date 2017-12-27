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

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KeyValueStore {
	private Map<String, Map<String, Object>> store = new HashMap<>();

	public void createInstance(String id) {
		store.put(id, new HashMap<>());
	}

	public void deleteInstance(String id) {
		store.remove(id);
	}

	public Object get(String id, String key) {
		Map<String, Object> instance = getStoreById(id);
		return instance.get(key);
	}

	public Object put(String id, String key, Object value) {
		Map<String, Object> instance = getStoreById(id);
		return instance.put(key, value);
	}

	public Object remove(String id, String key) {
		Map<String, Object> instance = getStoreById(id);
		return instance.remove(key);
	}

	private Map<String, Object> getStoreById(String id) {
		Map<String, Object> selectedStore = store.get(id);
		if (selectedStore == null) {
			throw new IllegalArgumentException("Invalid instance ID " + id + ".");
		}
		return selectedStore;
	}
}
