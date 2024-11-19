/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.cloud.sample.bookstore.web.security;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.sample.bookstore.web.service.UserService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class UserApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

	private UserService userService;

	public UserApplicationListener(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.userService.initializeUsers();
	}

}
