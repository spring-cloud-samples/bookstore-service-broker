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

import java.util.Collection;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;

import org.springframework.cloud.sample.bookstore.web.model.User;
import org.springframework.cloud.sample.bookstore.web.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RepositoryUserDetailsService implements ReactiveUserDetailsService {

	private final UserRepository userRepository;

	public RepositoryUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public Mono<UserDetails> findByUsername(String username) {
		return this.userRepository.findByUsername(username)
			.switchIfEmpty(Mono.error(new UsernameNotFoundException(username)))
			.flatMap((user) -> Mono.just(new CustomUserDetails(user)));
	}

	private static class CustomUserDetails implements UserDetails {

		private static final long serialVersionUID = 8772606763988236474L;

		private final User delegate;

		CustomUserDetails(User user) {
			this.delegate = user;
		}

		@Override
		public String getUsername() {
			return this.delegate.getUsername();
		}

		@Override
		public String getPassword() {
			return this.delegate.getPassword();
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return this.delegate.getAuthorities()
				.stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
		}

		@Override
		public boolean isAccountNonExpired() {
			return true;
		}

		@Override
		public boolean isAccountNonLocked() {
			return true;
		}

		@Override
		public boolean isCredentialsNonExpired() {
			return true;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public String toString() {
			return "CustomUserDetails{" + "username=" + getUsername() + ", password=" + getPassword() + ", authorities="
					+ getAuthorities() + '}';
		}

	}

}
