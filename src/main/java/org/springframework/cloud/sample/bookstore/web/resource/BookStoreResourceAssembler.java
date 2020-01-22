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

package org.springframework.cloud.sample.bookstore.web.resource;

import reactor.core.publisher.Mono;

import org.springframework.cloud.sample.bookstore.web.controller.BookStoreController;
import org.springframework.cloud.sample.bookstore.web.model.BookStore;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

public class BookStoreResourceAssembler {

	public Mono<BookStoreResource> toModel(BookStore bookStore) {
		return new BookResourceAssembler().toCollectionModel(bookStore.getBooks(), bookStore.getId())
				.flatMap(bookResources -> Mono.just(new BookStoreResource(bookResources))
						.flatMap(bookStoreResource -> linkTo(
								methodOn(BookStoreController.class).getBooks(bookStore.getId()))
								.withSelfRel().toMono()
								.flatMap(link -> Mono.just(bookStoreResource.add(link)))
								.thenReturn(bookStoreResource)));
	}

}
