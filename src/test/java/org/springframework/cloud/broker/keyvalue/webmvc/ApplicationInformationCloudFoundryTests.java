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

package org.springframework.cloud.broker.keyvalue.webmvc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.broker.keyvalue.webmvc.service.ApplicationInformation;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
				"VCAP_APPLICATION: {" +
				" \"application_id\": \"ede01bcb-bbc2-4bd4-ac7b-b56be151afc3\"," +
				" \"uris\": [" +
				"  \"route.apps.example.com\"," +
				"  \"other-route.apps.example.com\"" +
				" ]" +
				"}"
})
@TestConfiguration
public class ApplicationInformationCloudFoundryTests {
	@Autowired
	private ApplicationInformation appInfo;

	@Test
	public void applicationInfo() {
		assertThat(appInfo.getBaseUrl(), equalTo("https://route.apps.example.com"));
	}
}
