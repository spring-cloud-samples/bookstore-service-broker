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

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cloud.sample.bookstore.web.controller.BookController;
import org.springframework.cloud.sample.bookstore.web.controller.BookStoreController;
import org.springframework.cloud.sample.bookstore.web.model.Book;
import org.springframework.cloud.sample.bookstore.web.model.BookStore;
import org.springframework.cloud.sample.bookstore.web.repository.BookStoreRepository;
import org.springframework.cloud.sample.bookstore.web.service.BookStoreService;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@DataJpaTest
public class BookStoreIntegrationTests {

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	private WebTestClient client;

	@Autowired
	private BookStoreRepository repository;

	private String bookStoreId;

	@Before
	public void setUp() {
		BookStoreService service = new BookStoreService(repository);
		BookStoreController bookStoreController = new BookStoreController(service);
		BookController bookController = new BookController(service);
		this.client = WebTestClient.bindToController(bookStoreController, bookController)
				.build();

		BookStore bookStore = service.createBookStore();
		service.putBookInStore(bookStore.getId(),
				new Book("978-1617292545", "Spring Boot in Action", "Craig Walls"));
		service.putBookInStore(bookStore.getId(),
				new Book("978-1784393021", "Learning Spring Boot", "Greg L. Turnquist"));
		this.bookStoreId = bookStore.getId();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void bookStoreIsRetrieved() {
		BookStore bookStore = getBookStoreFromRepository();

		this.client.get().uri("/bookstores/{bookStoreId}", bookStore.getId())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.consumeWith(result -> {
					String responseBody = new String(result.getResponseBody(), UTF_8);
					List<Object> books = JsonPath.read(responseBody, "$.books");
					assertThat(books).hasSize(2);

					List<String> isbns = JsonPath.read(responseBody, "$.books[*].isbn");
					assertThat(isbns).hasSize(2);
					assertThat(isbns).containsExactlyInAnyOrder(
							bookStore.getBooks().get(0).getIsbn(),
							bookStore.getBooks().get(1).getIsbn());

					List<Object> bookLinks = JsonPath.read(responseBody, "$.books[*].links");
					assertThat(bookLinks).hasSize(2);

					List<String> hrefs = JsonPath.read(responseBody, "$.books[*].links[*].href");
					assertThat(hrefs).containsExactlyInAnyOrder(
							buildBookRef(bookStore.getId(), bookStore.getBooks().get(0).getId()),
							buildBookRef(bookStore.getId(), bookStore.getBooks().get(1).getId()));

					List<Object> links = JsonPath.read(responseBody, "$.links");
					assertThat(links).hasSize(1);

					String href = JsonPath.read(responseBody, "$.links[0].href");
					assertThat(href).endsWith(buildBookStoreRef(bookStore.getId()));

					String rel = JsonPath.read(responseBody, "$.links[0].rel");
					assertThat(rel).isEqualTo(Link.REL_SELF);
				});
	}

	@Test
	public void bookIsRetrieved() throws Exception {
		BookStore bookStore = getBookStoreFromRepository();
		Book book = bookStore.getBooks().get(0);

		this.client.get().uri("/bookstores/{bookStoreId}/books/{bookId}", bookStore.getId(), book.getId())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.consumeWith(result -> verifyBook(result, bookStore.getId(), book.getId(), book.getIsbn()));
	}

	@Test
	public void bookIsAdded() {
		BookStore bookStore = getBookStoreFromRepository();

		this.client.put().uri("/bookstores/{bookStoreId}/books", bookStore.getId())
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.syncBody("{\"isbn\":\"978-1785284151\", \"title\":\"Spring Boot Cookbook\", \"author\":\"Alex Antonov\"}")
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.consumeWith(result -> verifyBook(result, bookStore.getId(), "", "978-1785284151"));

		assertThat(getBooksFromRepository()).size().isEqualTo(3);
	}

	@Test
	public void bookIsDeleted() throws Exception {
		BookStore bookStore = getBookStoreFromRepository();
		Book book = bookStore.getBooks().get(0);

		this.client.delete().uri("/bookstores/{bookStoreId}/books/{bookId}", bookStore.getId(), book.getId())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.consumeWith(result -> verifyBook(result, bookStore.getId(), book.getId(), book.getIsbn()));

		assertThat(getBooksFromRepository()).size().isEqualTo(1);
	}

	private void verifyBook(EntityExchangeResult<byte[]> result, String bookStoreId, String bookId, String bookIsbn) {
		String responseBody = new String(result.getResponseBody(), UTF_8);

		String isbn = JsonPath.read(responseBody, "$.isbn");
		assertThat(isbn).isEqualTo(bookIsbn);

		List<Object> links = JsonPath.read(responseBody, "$.links");
		assertThat(links).hasSize(1);

		String href = JsonPath.read(responseBody, "$.links[0].href");
		assertThat(href).contains(bookStoreId);
		if (StringUtils.hasText(bookId)) {
			assertThat(href).endsWith(buildBookRef(bookStoreId, bookId));
		}

		String rel = JsonPath.read(responseBody, "$.links[0].rel");
		assertThat(rel).isEqualTo(Link.REL_SELF);
	}

	private List<Book> getBooksFromRepository() {
		return getBookStoreFromRepository().getBooks();
	}

	private BookStore getBookStoreFromRepository() {
		Optional<BookStore> bookStore = repository.findById(bookStoreId);

		if (!bookStore.isPresent()) {
			fail("bookstore not found in repository");
		}

		return bookStore.get();
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
