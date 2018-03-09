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

package org.springframework.cloud.sample.bookstore.servicebroker.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cloud.sample.bookstore.servicebroker.model.ServiceInstance;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@DataJpaTest
public class ServiceInstanceRepositoryTests {
	@Autowired
	private ServiceInstanceRepository repository;

	private final HashMap<String, Object> parameters = new HashMap<String, Object>() {{
		put("key1", "value1");
		put("key2", "value2");
	}};

	@Test
	public void save() {
		ServiceInstance instance = new ServiceInstance("service-instance-id", "service-definition-id",
				"plan-id", parameters);

		ServiceInstance savedInstance = repository.save(instance);

		assertThat(savedInstance).isEqualToComparingFieldByField(instance);
	}

	@Test
	public void retrieve() {
		ServiceInstance instance = new ServiceInstance("service-instance-id", "service-definition-id",
				"plan-id", parameters);

		repository.save(instance);

		Optional<ServiceInstance> foundInstance = repository.findById("service-instance-id");

		assertThat(foundInstance).isPresent();
		assertThat(foundInstance.orElse(null)).isEqualToComparingFieldByField(instance);
	}
}