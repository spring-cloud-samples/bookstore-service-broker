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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sample.bookstore.ServiceBrokerApplication;
import org.springframework.cloud.sample.bookstore.web.model.Book;
import org.springframework.cloud.sample.bookstore.web.model.BookStore;
import org.springframework.cloud.sample.bookstore.web.service.BookStoreService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.BOOK_STORE_ID_PREFIX;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.FULL_ACCESS;
import static org.springframework.cloud.sample.bookstore.web.security.SecurityAuthorities.READ_ONLY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ServiceBrokerApplication.class,
		BookStoreSecurityIntegrationTests.TestConfiguration.class})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class BookStoreSecurityIntegrationTests {
	private static final String BOOKSTORE_INSTANCE_ID = "1111-1111-1111-1111";
	private static final String OTHER_INSTANCE_ID = "2222-2222-2222-2222";

	@Autowired
	private BookStoreService bookStoreService;

	@Autowired
	private MockMvc mockMvc;

	private String bookStoreId;
	private String bookId;

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
		assertExpectedResponseStatus(
				status().isUnauthorized(),
				status().isUnauthorized(),
				status().isUnauthorized(),
				status().isUnauthorized());
	}

	@Test
	@WithMockUser(authorities = {FULL_ACCESS})
	public void fullAccessWithoutInstanceIdIsAllowed() throws Exception {
		assertExpectedResponseStatus(
				status().isOk(),
				status().isOk(),
				status().isCreated(),
				status().isOk());
	}

	@Test
	@WithMockUser(authorities = {FULL_ACCESS, BOOK_STORE_ID_PREFIX + BOOKSTORE_INSTANCE_ID})
	public void fullAccessWithInstanceIdIsAllowed() throws Exception {
		assertExpectedResponseStatus(
				status().isOk(),
				status().isOk(),
				status().isCreated(),
				status().isOk());
	}

	@Test
	@WithMockUser(authorities = {FULL_ACCESS, BOOK_STORE_ID_PREFIX + OTHER_INSTANCE_ID})
	public void fullAccessWithOtherInstanceIdIsForbidden() throws Exception {
		assertExpectedResponseStatus(
				status().isForbidden(),
				status().isForbidden(),
				status().isForbidden(),
				status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = {READ_ONLY})
	public void readOnlyWithoutInstanceIdIsPartiallyAllowed() throws Exception {
		assertExpectedResponseStatus(
				status().isOk(),
				status().isOk(),
				status().isForbidden(),
				status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = {READ_ONLY, BOOK_STORE_ID_PREFIX + BOOKSTORE_INSTANCE_ID})
	public void readOnlyWithInstanceIdIsPartiallyAllowed() throws Exception {
		assertExpectedResponseStatus(
				status().isOk(),
				status().isOk(),
				status().isForbidden(),
				status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = {READ_ONLY, BOOK_STORE_ID_PREFIX + OTHER_INSTANCE_ID})
	public void readOnlyWithOtherInstanceIdIsForbidden() throws Exception {
		assertExpectedResponseStatus(
				status().isForbidden(),
				status().isForbidden(),
				status().isForbidden(),
				status().isForbidden());
	}

	private void assertExpectedResponseStatus(ResultMatcher getAllStatus,
											  ResultMatcher getStatus,
											  ResultMatcher putStatus,
											  ResultMatcher deleteStatus) throws Exception {
		this.mockMvc.perform(get("/bookstores/{bookStoreId}", bookStoreId))
				.andExpect(getAllStatus);

		this.mockMvc.perform(get("/bookstores/{bookStoreId}/books/{bookId}",
				bookStoreId, bookId))
				.andExpect(getStatus);

		this.mockMvc.perform(put("/bookstores/{bookStoreId}/books", bookStoreId)
				.content("{\"isbn\":\"111-1111111111\", \"title\":\"test book\", \"author\":\"test author\"}"))
				.andExpect(putStatus);

		this.mockMvc.perform(delete("/bookstores/{bookStoreId}/books/{bookId}",
				bookStoreId, bookId))
				.andExpect(deleteStatus);
	}

	@Configuration
	static class TestConfiguration {
		@Bean
		MockMvcBuilderCustomizer mockMvcBuilderCustomizer() {
			return b -> b.defaultRequest(
				get("/")
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
			);
		}
	}
}
