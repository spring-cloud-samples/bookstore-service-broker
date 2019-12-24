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

package org.springframework.cloud.sample.bookstore.web.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sample.bookstore.web.model.Book;
import org.springframework.cloud.sample.bookstore.web.model.BookStore;
import org.springframework.cloud.sample.bookstore.web.service.BookStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.BOOK_STORE_ID_PREFIX;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.FULL_ACCESS;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.READ_ONLY;

@SpringBootTest
@AutoConfigureWebTestClient
public class BookStoreSecurityIntegrationTests {

	private static final String BOOKSTORE_INSTANCE_ID = "1111-1111-1111-1111";

	private static final String OTHER_INSTANCE_ID = "2222-2222-2222-2222";

	@Autowired
	private BookStoreService bookStoreService;

	@Autowired
	private WebTestClient client;

	private String bookStoreId;

	private String bookId;

	@BeforeEach
	public void setUp() {
		BookStore bookStore = bookStoreService.createBookStore(BOOKSTORE_INSTANCE_ID).block();
		this.bookStoreId = bookStore.getId();

		Book book = bookStoreService.putBookInStore(bookStoreId,
			new Book("978-1617292545", "Spring Boot in Action", "Craig Walls"))
			.block();
		this.bookId = book.getId();
	}

	@Test
	public void anonymousAccessIsUnauthorized() {
		assertExpectedResponseStatus(
			HttpStatus.UNAUTHORIZED,
			HttpStatus.UNAUTHORIZED,
			HttpStatus.UNAUTHORIZED,
			HttpStatus.UNAUTHORIZED);
	}

	@Test
	@WithMockUser(authorities = {FULL_ACCESS})
	public void fullAccessWithoutInstanceIdIsAllowed() {

		assertExpectedResponseStatus(
			HttpStatus.OK,
			HttpStatus.OK,
			HttpStatus.CREATED,
			HttpStatus.OK);
	}

	@Test
	@WithMockUser(authorities = {FULL_ACCESS, BOOK_STORE_ID_PREFIX + BOOKSTORE_INSTANCE_ID})
	public void fullAccessWithInstanceIdIsAllowed() {
		assertExpectedResponseStatus(
			HttpStatus.OK,
			HttpStatus.OK,
			HttpStatus.CREATED,
			HttpStatus.OK);
	}

	@Test
	@WithMockUser(authorities = {FULL_ACCESS, BOOK_STORE_ID_PREFIX + OTHER_INSTANCE_ID})
	public void fullAccessWithOtherInstanceIdIsForbidden() {
		assertExpectedResponseStatus(
			HttpStatus.FORBIDDEN,
			HttpStatus.FORBIDDEN,
			HttpStatus.FORBIDDEN,
			HttpStatus.FORBIDDEN);
	}

	@Test
	@WithMockUser(authorities = {READ_ONLY})
	public void readOnlyWithoutInstanceIdIsPartiallyAllowed() {
		assertExpectedResponseStatus(
			HttpStatus.OK,
			HttpStatus.OK,
			HttpStatus.FORBIDDEN,
			HttpStatus.FORBIDDEN);
	}

	@Test
	@WithMockUser(authorities = {READ_ONLY, BOOK_STORE_ID_PREFIX + BOOKSTORE_INSTANCE_ID})
	public void readOnlyWithInstanceIdIsPartiallyAllowed() {
		assertExpectedResponseStatus(
			HttpStatus.OK,
			HttpStatus.OK,
			HttpStatus.FORBIDDEN,
			HttpStatus.FORBIDDEN);
	}

	@Test
	@WithMockUser(authorities = {READ_ONLY, BOOK_STORE_ID_PREFIX + OTHER_INSTANCE_ID})
	public void readOnlyWithOtherInstanceIdIsForbidden() {
		assertExpectedResponseStatus(
			HttpStatus.FORBIDDEN,
			HttpStatus.FORBIDDEN,
			HttpStatus.FORBIDDEN,
			HttpStatus.FORBIDDEN);
	}

	private void assertExpectedResponseStatus(HttpStatus getAllStatus, HttpStatus getStatus, HttpStatus putStatus,
		HttpStatus deleteStatus) {
		this.client.get().uri("/bookstores/{bookStoreId}", bookStoreId)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(getAllStatus);

		this.client.get().uri("/bookstores/{bookStoreId}/books/{bookId}", bookStoreId, bookId)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(getStatus);

		this.client.put().uri("/bookstores/{bookStoreId}/books", bookStoreId)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"isbn\":\"111-1111111111\", \"title\":\"test book\", \"author\":\"test author\"}")
			.exchange()
			.expectStatus().isEqualTo(putStatus);

		this.client.delete().uri("/bookstores/{bookStoreId}/books/{bookId}", bookStoreId, bookId)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(deleteStatus);
	}

}
