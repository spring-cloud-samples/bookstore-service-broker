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

package org.springframework.cloud.broker.bookstore.webmvc.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

import static org.springframework.cloud.broker.bookstore.webmvc.security.SecurityAuthorities.SERVICE_INSTANCE_PREFIX;

public class ServiceInstancePermissionEvaluator implements PermissionEvaluator {
	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		Optional<Boolean> matched = authorities.stream()
				.filter(authority -> authority.getAuthority().startsWith(SERVICE_INSTANCE_PREFIX))
				.map(authority -> {
					String serviceInstanceId = authority.getAuthority().substring(SERVICE_INSTANCE_PREFIX.length());
					return serviceInstanceId.equals(targetDomainObject);
				})
				.findFirst();

		return matched.orElse(true);
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
		return true;
	}
}
