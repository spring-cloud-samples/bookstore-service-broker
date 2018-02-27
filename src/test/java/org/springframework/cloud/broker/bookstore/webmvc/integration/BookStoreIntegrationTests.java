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

package org.springframework.cloud.broker.bookstore.webmvc.integration;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.broker.bookstore.webmvc.controller.BookController;
import org.springframework.cloud.broker.bookstore.webmvc.controller.BookStoreController;
import org.springframework.cloud.broker.bookstore.webmvc.model.Book;
import org.springframework.cloud.broker.bookstore.webmvc.model.BookStore;
import org.springframework.cloud.broker.bookstore.webmvc.service.BookStoreService;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BookStoreIntegrationTests {
	private MockMvc mockMvc;

	private BookStore bookStore;

	@Before
	public void setUp() {
		BookStoreService service = new BookStoreService();

		BookStoreController bookStoreController = new BookStoreController(service);
		BookController bookController = new BookController(service);

		mockMvc = MockMvcBuilders
				.standaloneSetup(bookStoreController, bookController)
				.defaultRequest(get("/")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON))
				.alwaysExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.setMessageConverters(new MappingJackson2HttpMessageConverter())
				.build();

		bookStore = service.createBookStore();
		service.putBookInStore(bookStore.getId(),
				new Book("978-1617292545", "Spring Boot in Action", "Craig Walls"));
		service.putBookInStore(bookStore.getId(),
				new Book("978-1784393021", "Learning Spring Boot", "Greg L. Turnquist"));
	}

	@Test
	public void bookStoreIsRetrieved() throws Exception {

		this.mockMvc.perform(get("/bookstores/{bookStoreId}", bookStore.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.books", hasSize(2)))

				.andExpect(jsonPath("$.books[*].isbn", hasSize(2)))
				.andExpect(jsonPath("$.books[*].isbn", containsInAnyOrder(
						bookStore.getBooks().get(0).getIsbn(),
						bookStore.getBooks().get(1).getIsbn()
				)))

				.andExpect(jsonPath("$.books[*].links[*]", hasSize(2)))
				.andExpect(jsonPath("$.books[*].links[*].href", containsInAnyOrder(
						endsWith(buildBookRef(bookStore.getId(), bookStore.getBooks().get(0).getId())),
						endsWith(buildBookRef(bookStore.getId(), bookStore.getBooks().get(1).getId()))
				)))
				
				.andExpect(jsonPath("$.links", hasSize(1)))
				.andExpect(jsonPath("$.links[0].href", endsWith(buildBookStoreRef(bookStore.getId()))))
				.andExpect(jsonPath("$.links[0].rel", equalTo(Link.REL_SELF)));
	}

	@Test
	public void bookIsRetrieved() throws Exception {
		Book book = bookStore.getBooks().get(0);

		this.mockMvc.perform(get("/bookstores/{bookStoreId}/books/{bookId}", bookStore.getId(), book.getId()))
				.andExpect(status().isOk())

				.andExpect(jsonPath("$.isbn", equalTo(book.getIsbn())))

				.andExpect(jsonPath("$.links", hasSize(1)))
				.andExpect(jsonPath("$.links[0].href", endsWith(buildBookRef(bookStore.getId(), book.getId()))))
				.andExpect(jsonPath("$.links[0].rel", equalTo(Link.REL_SELF)));
	}

	@Test
	public void bookIsAdded() throws Exception {
		this.mockMvc.perform(put("/bookstores/{bookStoreId}/books", bookStore.getId())
				.content("{\"isbn\":\"978-1785284151\", \"title\":\"Spring Boot Cookbook\", \"author\":\"Alex Antonov\"}"))
				.andExpect(status().isCreated())

				.andExpect(jsonPath("$.isbn", equalTo("978-1785284151")))

				.andExpect(jsonPath("$.links", hasSize(1)))
				.andExpect(jsonPath("$.links[0].href", containsString(buildBookRef(bookStore.getId()))))
				.andExpect(jsonPath("$.links[0].rel", equalTo(Link.REL_SELF)));

		assertThat(bookStore).size().isEqualTo(3);
	}

	@Test
	public void bookIsDeleted() throws Exception {
		Book book = bookStore.getBooks().get(0);

		this.mockMvc.perform(delete("/bookstores/{bookStoreId}/books/{bookId}", bookStore.getId(), book.getId()))
				.andExpect(status().isOk())

				.andExpect(jsonPath("$.isbn", equalTo(book.getIsbn())))

				.andExpect(jsonPath("$.links", hasSize(1)))
				.andExpect(jsonPath("$.links[0].href", endsWith(buildBookRef(bookStore.getId(), book.getId()))))
				.andExpect(jsonPath("$.links[0].rel", equalTo(Link.REL_SELF)));

		assertThat(bookStore).size().isEqualTo(1);
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
