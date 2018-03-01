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

package org.springframework.cloud.sample.bookstore.webmvc.integration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sample.bookstore.ServiceBrokerApplication;
import org.springframework.cloud.sample.bookstore.webmvc.model.Book;
import org.springframework.cloud.sample.bookstore.webmvc.model.BookStore;
import org.springframework.cloud.sample.bookstore.webmvc.security.SecurityAuthorities;
import org.springframework.cloud.sample.bookstore.webmvc.service.BookStoreService;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.cloud.sample.bookstore.webmvc.security.SecurityAuthorities.BOOK_STORE_ID_PREFIX;
import static org.springframework.cloud.sample.bookstore.webmvc.security.SecurityAuthorities.ROLE_FULL_ACCESS;
import static org.springframework.cloud.sample.bookstore.webmvc.security.SecurityAuthorities.ROLE_READ_ONLY;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ServiceBrokerApplication.class})
@WebAppConfiguration
public class BookStoreSecurityIntegrationTests {
	private static final String BOOKSTORE_INSTANCE_ID = "1111-1111-1111-1111";
	private static final String OTHER_INSTANCE_ID = "2222-2222-2222-2222";

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private BookStoreService bookStoreService;

	private BookStore bookStore;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders
				.webAppContextSetup(context)
				.defaultRequest(get("/")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON))
				.apply(springSecurity())
				.build();

		bookStore = bookStoreService.createBookStore(BOOKSTORE_INSTANCE_ID);
		bookStoreService.putBookInStore(bookStore.getId(),
				new Book("978-1617292545", "Spring Boot in Action", "Craig Walls"));
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
	@WithMockUser(authorities = {ROLE_FULL_ACCESS})
	public void fullAccessWithoutInstanceIdIsAllowed() throws Exception {
		assertExpectedResponseStatus(
				status().isOk(),
				status().isOk(),
				status().isCreated(),
				status().isOk());
	}

	@Test
	@WithMockUser(authorities = {ROLE_FULL_ACCESS, BOOK_STORE_ID_PREFIX + BOOKSTORE_INSTANCE_ID})
	public void fullAccessWithInstanceIdIsAllowed() throws Exception {
		assertExpectedResponseStatus(
				status().isOk(),
				status().isOk(),
				status().isCreated(),
				status().isOk());
	}

	@Test
	@WithMockUser(authorities = {ROLE_FULL_ACCESS, BOOK_STORE_ID_PREFIX + OTHER_INSTANCE_ID})
	public void fullAccessWithOtherInstanceIdIsForbidden() throws Exception {
		assertExpectedResponseStatus(
				status().isForbidden(),
				status().isForbidden(),
				status().isForbidden(),
				status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = {ROLE_READ_ONLY})
	public void readOnlyWithoutInstanceIdIsPartiallyAllowed() throws Exception {
		assertExpectedResponseStatus(
				status().isOk(),
				status().isOk(),
				status().isForbidden(),
				status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = {ROLE_READ_ONLY, BOOK_STORE_ID_PREFIX + BOOKSTORE_INSTANCE_ID})
	public void readOnlyWithInstanceIdIsPartiallyAllowed() throws Exception {
		assertExpectedResponseStatus(
				status().isOk(),
				status().isOk(),
				status().isForbidden(),
				status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = {ROLE_READ_ONLY, BOOK_STORE_ID_PREFIX + OTHER_INSTANCE_ID})
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
		this.mockMvc.perform(get("/bookstores/{bookStoreId}", bookStore.getId()))
				.andExpect(getAllStatus);

		this.mockMvc.perform(get("/bookstores/{bookStoreId}/books/{bookId}",
				bookStore.getId(), bookStore.getBooks().get(0).getId()))
				.andExpect(getStatus);

		this.mockMvc.perform(put("/bookstores/{bookStoreId}/books", bookStore.getId())
				.content("{\"isbn\":\"111-1111111111\", \"title\":\"test book\", \"author\":\"test author\"}"))
				.andExpect(putStatus);

		this.mockMvc.perform(delete("/bookstores/{bookStoreId}/books/{bookId}",
				bookStore.getId(), bookStore.getBooks().get(0).getId()))
				.andExpect(deleteStatus);
	}
}
