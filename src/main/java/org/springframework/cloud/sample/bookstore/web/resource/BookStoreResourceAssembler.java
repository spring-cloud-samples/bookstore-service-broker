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

package org.springframework.cloud.sample.bookstore.web.resource;

import org.springframework.cloud.sample.bookstore.web.controller.BookStoreController;
import org.springframework.cloud.sample.bookstore.web.model.BookStore;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class BookStoreResourceAssembler {
	public BookStoreResource toResource(BookStore bookStore) {
		BookResourceAssembler bookAssembler = new BookResourceAssembler();
		List<BookResource> bookResources = bookAssembler.toResources(bookStore.getBooks(), bookStore.getId());

		BookStoreResource bookStoreResource = new BookStoreResource(bookResources);
		bookStoreResource.add(
				linkTo(BookStoreController.class)
						.slash(bookStore.getId())
						.withSelfRel());
		return bookStoreResource;
	}
}
