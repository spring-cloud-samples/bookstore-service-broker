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

package org.springframework.cloud.sample.bookstore.web.integration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sample.bookstore.ServiceBrokerApplication;
import org.springframework.cloud.sample.bookstore.web.model.Book;
import org.springframework.cloud.sample.bookstore.web.model.BookStore;
import org.springframework.cloud.sample.bookstore.web.model.User;
import org.springframework.cloud.sample.bookstore.web.service.BookStoreService;
import org.springframework.cloud.sample.bookstore.web.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.BOOK_STORE_ID_PREFIX;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.FULL_ACCESS;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.READ_ONLY;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ServiceBrokerApplication.class, }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class BookStoreSecurityIntegrationTests {

	private static final String BOOKSTORE_INSTANCE_ID = "1111-1111-1111-1111";
	private static final String OTHER_INSTANCE_ID = "2222-2222-2222-2222";
	private String bookId;
	private String bookStoreId;
	@Autowired
	private BookStoreService bookStoreService;
	@Autowired
	private WebTestClient client;
	@Autowired
	private UserService userService;

	@Before
	public void setUp() {

		BookStore bookStore = bookStoreService.createBookStore(BOOKSTORE_INSTANCE_ID);
		bookStoreId = bookStore.getId();

		Book book = bookStoreService.putBookInStore(bookStoreId,
				new Book("978-1617292545", "Spring Boot in Action", "Craig Walls"));
		bookId = book.getId();

	}

	@Test
	public void anonymousAccessIsUnauthorized() throws Exception {

		User dummyUser = new User("dummyuser", "dummypassword", "NO_AUTHORITY");
		assertExpectedResponseStatus(
				HttpStatus.UNAUTHORIZED,
				HttpStatus.UNAUTHORIZED,
				HttpStatus.UNAUTHORIZED,
				HttpStatus.UNAUTHORIZED,
				dummyUser);
	}

	@Test
	public void fullAccessWithInstanceIdIsAllowed() throws Exception {

		User fullAccessUserWithInstanceId = userService
				.createUser("fullAccessUserWithInstanceId", FULL_ACCESS, BOOK_STORE_ID_PREFIX + BOOKSTORE_INSTANCE_ID);

		assertExpectedResponseStatus(
				HttpStatus.OK,
				HttpStatus.OK,
				HttpStatus.CREATED,
				HttpStatus.OK,
				fullAccessUserWithInstanceId);
	}

	@Test
	public void fullAccessWithOtherInstanceIdIsForbidden() throws Exception {
		User fullAccessUserWithOtherId = userService
				.createUser("fullAccessUserWithOtherId", FULL_ACCESS, BOOK_STORE_ID_PREFIX + OTHER_INSTANCE_ID);

		assertExpectedResponseStatus(
				HttpStatus.FORBIDDEN,
				HttpStatus.FORBIDDEN,
				HttpStatus.FORBIDDEN,
				HttpStatus.FORBIDDEN,
				fullAccessUserWithOtherId);
	}

	@Test
	public void fullAccessWithoutInstanceIdIsAllowed() throws Exception {
		User fullAccessUser = userService.createUser("fullAccessUser", FULL_ACCESS);

		assertExpectedResponseStatus(
				HttpStatus.OK,
				HttpStatus.OK,
				HttpStatus.CREATED,
				HttpStatus.OK,
				fullAccessUser);
	}

	@Test
	public void readOnlyWithInstanceIdIsPartiallyAllowed() throws Exception {
		User readOnlyUserwithInstanceId = userService
				.createUser("readOnlyUserwithInstanceId", READ_ONLY, BOOK_STORE_ID_PREFIX + BOOKSTORE_INSTANCE_ID);

		assertExpectedResponseStatus(
				HttpStatus.OK,
				HttpStatus.OK,
				HttpStatus.FORBIDDEN,
				HttpStatus.FORBIDDEN,
				readOnlyUserwithInstanceId);
	}

	@Test
	public void readOnlyWithOtherInstanceIdIsForbidden() throws Exception {
		User readOnlyUserOtherInstanceId = userService
				.createUser("readOnlyUserOtherInstanceId", READ_ONLY, BOOK_STORE_ID_PREFIX + OTHER_INSTANCE_ID);

		assertExpectedResponseStatus(
				HttpStatus.FORBIDDEN,
				HttpStatus.FORBIDDEN,
				HttpStatus.FORBIDDEN,
				HttpStatus.FORBIDDEN,
				readOnlyUserOtherInstanceId);
	}

	@Test
	public void readOnlyWithoutInstanceIdIsPartiallyAllowed() throws Exception {
		User readonlyUserNoScope = userService.createUser("readonlyUserNoScope", READ_ONLY);

		assertExpectedResponseStatus(
				HttpStatus.OK,
				HttpStatus.OK,
				HttpStatus.FORBIDDEN,
				HttpStatus.FORBIDDEN,
				readonlyUserNoScope);
	}


	private void assertExpectedResponseStatus(HttpStatus getAllStatus, HttpStatus getStatus, HttpStatus putStatus,
			HttpStatus deleteStatus, User user) {

		WebTestClient authenticatedClient = this.client
				.mutate()
				.filter(basicAuthentication(user.getUsername(), user.getPassword()))
				.build();

		authenticatedClient
				.get().uri("/bookstores/{bookStoreId}", bookStoreId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(getAllStatus);

		authenticatedClient
				.get().uri("/bookstores/{bookStoreId}/books/{bookId}", bookStoreId, bookId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(getStatus);

		authenticatedClient
				.put().uri("/bookstores/{bookStoreId}/books", bookStoreId)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.syncBody("{\"isbn\":\"111-1111111111\", \"title\":\"test book\", \"author\":\"test author\"}")
				.exchange()
				.expectStatus().isEqualTo(putStatus);

		authenticatedClient
				.delete().uri("/bookstores/{bookStoreId}/books/{bookId}", bookStoreId, bookId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(deleteStatus);
	}

}
