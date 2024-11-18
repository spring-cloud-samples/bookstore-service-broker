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

package org.springframework.cloud.sample.bookstore.web.controller;

import java.util.Map;

import reactor.core.publisher.Mono;

import org.springframework.cloud.sample.bookstore.web.model.BookStore;
import org.springframework.cloud.sample.bookstore.web.resource.BookStoreResource;
import org.springframework.cloud.sample.bookstore.web.resource.BookStoreResourceAssembler;
import org.springframework.cloud.sample.bookstore.web.service.BookStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookstores")
public class BookStoreController extends BaseController {

	private final BookStoreService bookStoreService;

	public BookStoreController(BookStoreService bookStoreService) {
		this.bookStoreService = bookStoreService;
	}

	@GetMapping("/{bookStoreId}")
	@PreAuthorize("hasAnyRole('ROLE_FULL_ACCESS','ROLE_READ_ONLY') and @bookStoreIdEvaluator.canAccessBookstore"
			+ "(authentication, #bookStoreId)")
	public Mono<ResponseEntity<BookStoreResource>> getBooks(@PathVariable String bookStoreId) {
		return bookStoreService.getBookStore(bookStoreId).flatMap(this::createResponse);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public Mono<ResponseEntity<Map<String, String>>> badBookStoreId(IllegalArgumentException e) {
		return super.badBookStoreId(e);
	}

	private Mono<ResponseEntity<BookStoreResource>> createResponse(BookStore bookStore) {
		return new BookStoreResourceAssembler().toModel(bookStore)
			.flatMap(bookStoreResource -> Mono.just(new ResponseEntity<>(bookStoreResource, HttpStatus.OK)));
	}

}
