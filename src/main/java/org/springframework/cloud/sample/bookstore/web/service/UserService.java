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

package org.springframework.cloud.sample.bookstore.web.service;

import java.security.SecureRandom;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.cloud.sample.bookstore.web.model.User;
import org.springframework.cloud.sample.bookstore.web.repository.UserRepository;
import org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private static final String PASSWORD_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private static final int PASSWORD_LENGTH = 12;

	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public void initializeUsers() {
		if (userRepository.count() == 0) {
			userRepository.save(adminUser());
		}
	}

	public Mono<User> createUser(String username, String... authorities) {
		return generatePassword()
			.flatMap(password -> Mono.fromCallable(() -> passwordEncoder.encode(password))
				.flatMap(encodedPassword -> saveUser(new User(username, encodedPassword, authorities)))
				.thenReturn(new User(username, password, authorities)));
	}

	public Mono<Void> deleteUser(String username) {
		return findByUsername(username)
			.flatMap(user -> deleteById(user.getId()));
	}

	private Mono<User> saveUser(User user) {
		return Mono.fromCallable(() -> userRepository.save(user))
			.subscribeOn(Schedulers.boundedElastic());
	}

	private Mono<User> findByUsername(String username) {
		return Mono.justOrEmpty(username)
			.flatMap(nonEmptyUsername -> Mono.fromCallable(() -> userRepository.findByUsername(nonEmptyUsername))
				.subscribeOn(Schedulers.boundedElastic()));
	}

	private Mono<Void> deleteById(Long userId) {
		return Mono.justOrEmpty(userId)
			.flatMap(nonEmptyUserId -> Mono.fromCallable(() -> {
				userRepository.deleteById(nonEmptyUserId);
				return null;
			})
				.subscribeOn(Schedulers.boundedElastic()))
			.then();
	}

	private User adminUser() {
		return new User("admin", passwordEncoder.encode("supersecret"),
			SecurityAuthorities.ADMIN, SecurityAuthorities.FULL_ACCESS);
	}

	private Mono<String> generatePassword() {
		return Mono.just(new StringBuilder(PASSWORD_LENGTH))
			.flatMap(sb -> Mono.fromCallable(() -> {
				for (int i = 0; i < PASSWORD_LENGTH; i++) {
					sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
				}
				return null;
			})
				.subscribeOn(Schedulers.boundedElastic())
				.thenReturn(sb.toString()));
	}

}