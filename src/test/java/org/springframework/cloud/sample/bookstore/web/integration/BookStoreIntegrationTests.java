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

import java.util.Collection;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.cloud.sample.bookstore.web.controller.BookController;
import org.springframework.cloud.sample.bookstore.web.controller.BookStoreController;
import org.springframework.cloud.sample.bookstore.web.model.Book;
import org.springframework.cloud.sample.bookstore.web.model.BookStore;
import org.springframework.cloud.sample.bookstore.web.repository.BookStoreRepository;
import org.springframework.cloud.sample.bookstore.web.service.BookStoreService;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class BookStoreIntegrationTests {

	private WebTestClient client;

	@Autowired
	private BookStoreRepository repository;

	private BookStoreService service;

	private String bookStoreId;

	@BeforeEach
	public void setUp() {
		this.service = new BookStoreService(repository);

		BookStoreController bookStoreController = new BookStoreController(service);
		BookController bookController = new BookController(service);

		this.client = WebTestClient.bindToController(bookStoreController, bookController)
			.build();

		this.bookStoreId = service.createBookStore()
			.flatMap(bookStore -> service.putBookInStore(bookStore.getId(),
				new Book("978-1617292545", "Spring Boot in Action", "Craig Walls"))
				.then(service.putBookInStore(bookStore.getId(),
					new Book("978-1784393021", "Learning Spring Boot", "Greg L. Turnquist")))
				.thenReturn(bookStore.getId()))
			.block();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void bookStoreIsRetrieved() {
		BookStore bookStore = service.getBookStore(bookStoreId).block();

		String body = client.get().uri("/bookstores/{bookStoreId}", bookStore.getId())
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(body).isNotNull();
		ReadContext ctx = JsonPath.parse(body);
		assertThat(ctx.read("$.books", Collection.class)).hasSize(2);
		assertThat(ctx.read("$.books[*].isbn", Collection.class)).hasSize(2)
			.containsExactlyInAnyOrder(bookStore.getBooks().get(0).getIsbn(),
				bookStore.getBooks().get(1).getIsbn());
		assertThat(ctx.read("$.books[*].links[*]", Collection.class)).hasSize(2);
		assertThat(ctx.read("$.books[*].links[*].href", Collection.class)).containsExactlyInAnyOrder(
			buildBookRef(bookStore.getId(), bookStore.getBooks().get(0).getId()),
			buildBookRef(bookStore.getId(), bookStore.getBooks().get(1).getId()));
		assertThat(ctx.read("$.links", Collection.class)).hasSize(1);
		assertThat(ctx.read("$.links[0].href", String.class)).endsWith(buildBookStoreRef(bookStore.getId()));
		assertThat(ctx.read("$.links[0].rel", String.class)).isEqualTo(IanaLinkRelations.SELF.value());
	}

	@Test
	public void bookIsRetrieved() {
		BookStore bookStore = service.getBookStore(bookStoreId).block();
		Book book = bookStore.getBooks().get(0);

		String body = client.get().uri("/bookstores/{bookStoreId}/books/{bookId}", bookStore.getId(), book.getId())
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(body).isNotNull();
		ReadContext ctx = JsonPath.parse(body);
		assertThat(ctx.read("$.isbn", String.class)).isEqualTo(book.getIsbn());
		assertThat(ctx.read("$.links", Collection.class)).hasSize(1);
		assertThat(ctx.read("$.links[0].href", String.class)).endsWith(buildBookRef(bookStore.getId(), book.getId()));
		assertThat(ctx.read("$.links[0].rel", String.class)).isEqualTo(IanaLinkRelations.SELF.value());

		BookStore updatedBokStore = service.getBookStore(bookStoreId).block();
		assertThat(updatedBokStore.getBooks()).size().isEqualTo(2);
	}

	@Test
	public void bookIsAdded() {
		BookStore bookStore = service.getBookStore(bookStoreId).block();

		String body = client.put().uri("/bookstores/{bookStoreId}/books", bookStore.getId())
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue("{\"isbn\":\"978-1785284151\", \"title\":\"Spring Boot Cookbook\", \"author\":\"Alex Antonov\"}")
			.exchange()
			.expectStatus().isCreated()
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(body).isNotNull();
		ReadContext ctx = JsonPath.parse(body);
		assertThat(ctx.read("$.isbn", String.class)).isEqualTo("978-1785284151");
		assertThat(ctx.read("$.links", Collection.class)).hasSize(1);
		assertThat(ctx.read("$.links[0].href", String.class)).contains(buildBookRef(bookStore.getId()));
		assertThat(ctx.read("$.links[0].rel", String.class)).isEqualTo(IanaLinkRelations.SELF.value());

		BookStore updatedBokStore = service.getBookStore(bookStoreId).block();
		assertThat(updatedBokStore.getBooks()).size().isEqualTo(3);
	}

	@Test
	public void bookIsDeleted() {
		BookStore bookStore = service.getBookStore(bookStoreId).block();
		Book book = bookStore.getBooks().get(0);

		String body = client.delete().uri("/bookstores/{bookStoreId}/books/{bookId}", bookStore.getId(), book.getId())
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(body).isNotNull();
		ReadContext ctx = JsonPath.parse(body);
		assertThat(ctx.read("$.isbn", String.class)).isEqualTo(book.getIsbn());
		assertThat(ctx.read("$.links", Collection.class)).hasSize(1);
		assertThat(ctx.read("$.links[0].href", String.class)).endsWith(buildBookRef(bookStore.getId(), book.getId()));
		assertThat(ctx.read("$.links[0].rel", String.class)).isEqualTo(IanaLinkRelations.SELF.value());

		BookStore updatedBookStore = service.getBookStore(bookStoreId).block();
		assertThat(updatedBookStore.getBooks()).size().isEqualTo(1);
	}

	private String buildBookStoreRef(String bookStoreId) {
		return "/bookstores/" + bookStoreId;
	}

	private String buildBookRef(String bookStoreId) {
		return buildBookStoreRef(bookStoreId) + "/books/";
	}

	private String buildBookRef(String bookStoreId, String bookId) {
		return buildBookRef(bookStoreId) + bookId;
	}

}
