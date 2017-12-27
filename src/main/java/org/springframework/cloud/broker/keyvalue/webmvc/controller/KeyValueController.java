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

package org.springframework.cloud.broker.keyvalue.webmvc.controller;

import org.springframework.cloud.broker.keyvalue.webmvc.service.KeyValueStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/keyvalue")
public class KeyValueController {
	private final KeyValueStore store;

	public KeyValueController(KeyValueStore store) {
		this.store = store;
	}

	@GetMapping("/{instanceId}/{key}")
	public Map<String, Object> get(@PathVariable String instanceId, @PathVariable String key) {
		return Collections.singletonMap(key, store.get(instanceId, key));
	}

	@PutMapping("/{instanceId}/{key}")
	public Map<String, Object> put(@PathVariable String instanceId, @PathVariable String key, @RequestBody Object value) {
		store.put(instanceId, key, value);
		return Collections.singletonMap(key, store.get(instanceId, key));
	}

	@DeleteMapping("/{instanceId}/{key}")
	public Map<String, Object> delete(@PathVariable String instanceId, @PathVariable String key) {
		return Collections.singletonMap(key, store.remove(instanceId, key));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> badInstanceId(IllegalArgumentException e) {
		Map<String, String> responseBody = Collections.singletonMap("errorMessage", e.getMessage());
		return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
	}
}
