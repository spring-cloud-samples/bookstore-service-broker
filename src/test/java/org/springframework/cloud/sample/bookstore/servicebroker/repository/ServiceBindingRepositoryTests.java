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

package org.springframework.cloud.sample.bookstore.servicebroker.repository;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.cloud.sample.bookstore.servicebroker.model.ServiceBinding;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataMongoTest
public class ServiceBindingRepositoryTests {

	@Autowired
	private ServiceBindingRepository repository;

	private final HashMap<String, Object> parameters = new HashMap<String, Object>() {{
		put("key1", "value1");
		put("key2", "value2");
	}};

	private final HashMap<String, Object> credentials = new HashMap<String, Object>() {{
		put("url", "https://example.com");
		put("username", "user");
		put("password", "secret");
	}};

	@Test
	public void save() {
		ServiceBinding binding = new ServiceBinding("binding-id", parameters, credentials);

		StepVerifier.create(repository.save(binding))
			.assertNext(savedBinding -> assertThat(savedBinding).isEqualToComparingFieldByField(binding))
			.verifyComplete();
	}

	@Test
	public void retrieve() {
		ServiceBinding binding = new ServiceBinding("binding-id", parameters, credentials);

		StepVerifier.create(repository.save(binding))
			.expectNext(binding)
			.verifyComplete();

		StepVerifier.create(repository.findById("binding-id"))
			.assertNext(foundBinding -> assertThat(foundBinding).isEqualToComparingFieldByField(binding))
			.verifyComplete();
	}

}
