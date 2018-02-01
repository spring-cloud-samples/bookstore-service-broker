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
	private final Map<String, KeyValueMap> maps = new HashMap<>();

	public void createMap(String id) {
		maps.put(id, new KeyValueMap());
	}

	private KeyValueMap getMap(String id) {
		KeyValueMap selectedStore = maps.get(id);
		if (selectedStore == null) {
			throw new IllegalArgumentException("Invalid instance ID " + id + ".");
		}
		return selectedStore;
	}

	public void deleteMap(String id) {
		maps.remove(id);
	}

	public Object getValueFromMap(String mapId, String key) {
		KeyValueMap map = getMap(mapId);
		return map.get(key);
	}

	public Object putValueInMap(String mapId, String key, Object value) {
		KeyValueMap map = getMap(mapId);
		return map.put(key, value);
	}

	public Object removeValueFromMap(String mapId, String key) {
		Map<String, Object> map = getMap(mapId);
		return map.remove(key);
	}

	private static class KeyValueMap extends HashMap<String, Object> {
	}
}
